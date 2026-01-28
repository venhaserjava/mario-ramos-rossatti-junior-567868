package org.venhaserjava.service;


import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.venhaserjava.model.Album;
import org.venhaserjava.model.Artista;
import org.venhaserjava.websocket.ArtistaWebSocket;

import java.util.List;

@ApplicationScoped
public class ArtistaService {

    @Inject
    ArtistaWebSocket webSocket;

    /**
     * Requisitos 21, 22 e 23: Consulta com paginação, filtros e ordenação
     */
    public Uni<List<Artista>> listarComFiltros(int page, int size, String nome, String tipo, String order) {
        StringBuilder query = new StringBuilder("select distinct a from Artista a left join fetch a.albuns where 1=1");
        Parameters params = new Parameters();

        if (nome != null && !nome.isBlank()) {
            query.append(" and lower(a.nome) like lower(:nome)");
            params.and("nome", "%" + nome + "%");
        }

        if (tipo != null && !tipo.isBlank()) {
            query.append(" and a.tipo = :tipo");
            params.and("tipo", tipo);
        }

        Sort sort = "desc".equalsIgnoreCase(order) ? Sort.descending("nome") : Sort.ascending("nome");

        return Artista.find(query.toString(), sort, params)
                .page(page, size)
                .list();
    }
    @SuppressWarnings({ "unchecked", "removal" })
    @WithTransaction
    public Uni<Artista> salvar(Artista artista) {
        if (artista.albuns == null || artista.albuns.isEmpty()) {
            return Artista.persist(artista).replaceWith(artista);
        }

        return Artista.getSession().flatMap(session -> {
            // O segredo: usamos o session.merge para cada álbum da lista
            List<Uni<Album>> albumUnis = artista.albuns.stream()
                    .map(album -> session.merge(album)) 
                    .toList();

            return Uni.combine().all().unis(albumUnis)                    
                    .combinedWith(list -> {
                        artista.albuns = (List<Album>) (Object) list;
                        return artista;
                    })
                    .flatMap(a -> session.persist(a).replaceWith(a));
        }).onItem().invoke(a -> webSocket.broadcast("Novo artista cadastrado com álbuns: " + a.nome)); // Notificação N:N
        
    }

    @SuppressWarnings({ "unchecked", "removal" })
    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.<Artista>findById(id)
            .onItem().ifNotNull().transformToUni(existente -> {
                existente.nome = artista.nome;
                existente.tipo = artista.tipo;

                if (artista.albuns != null) {
                    return Artista.getSession().flatMap(session -> {
                        List<Uni<Album>> albumUnis = artista.albuns.stream()
                            .map(session::merge)
                            .toList();

                        return Uni.combine().all().unis(albumUnis)
                            .combinedWith(list -> {
                                List<Album> mergedAlbuns = (List<Album>) list;
                                existente.albuns = mergedAlbuns;
                                return existente;
                            });
                    });
                }
                return Uni.createFrom().item(existente);
            })
            // Notificação de atualização também via WebSocket
            .onItem().ifNotNull().invoke(a -> webSocket.broadcast("Artista atualizado: " + a.nome));
    }


    @WithTransaction
    public Uni<Boolean> deletar(Long id) {
        return Artista.getSession().flatMap(session -> 
            // 1. Primeiro, identificamos os IDs dos álbuns que pertencem APENAS a este artista
            session.createNativeQuery(
                "SELECT album_id FROM artista_album WHERE artista_id = :id " +
                "AND album_id NOT IN (SELECT album_id FROM artista_album WHERE artista_id <> :id)", Long.class)
            .setParameter("id", id)
            .getResultList()
            .flatMap(idsOrfaos -> {
                // 2. Deletamos o Artista (O CASCADE do banco limpa a artista_album automaticamente)
                return Artista.deleteById(id)
                    .flatMap(deletado -> {
                        if (deletado && !idsOrfaos.isEmpty()) {
                            // 3. Se houver álbuns órfãos, deletamos todos de uma vez
                            return session.createNativeQuery("DELETE FROM albuns WHERE id IN (:ids)")
                                    .setParameter("ids", idsOrfaos)
                                    .executeUpdate()
                                    .replaceWith(true);
                        }
                        return Uni.createFrom().item(deletado);
                    });
            })
        ).onItem().invoke(deletado -> {
            if (deletado) {
                webSocket.broadcast("Artista e álbuns exclusivos removidos. ID: " + id);
            }
        });
    }
}
