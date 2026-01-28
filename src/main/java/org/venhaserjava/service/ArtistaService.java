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

    @WithTransaction
    public Uni<Artista> salvar(Artista artista) {
        return Artista.persist(artista)
                .replaceWith(artista)
                // Requisito 61: Notificação via WebSocket
                .onItem().invoke(a -> webSocket.broadcast("Novo artista cadastrado: " + a.nome));
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


/*    
@WithTransaction
public Uni<Boolean> deletar(Long id) {
    // 1. Primeiro, buscamos apenas os IDs dos álbuns vinculados a este artista
    // Usamos HQL para não depender de campos da entidade que a IDE não está mapeando bem
    return Artista.getSession().flatMap(session -> 
        session.createQuery("select alb.id from Artista a join a.albuns alb where a.id = :id", Long.class)
               .setParameter("id", id)
               .getResultList()
               .flatMap(albumIds -> {
                   if (albumIds.isEmpty()) {
                       // Se não tem álbuns, deleta o artista direto
                       return Artista.deleteById(id);
                   }

                   // 2. Para cada álbum, verifica se ele ficará órfão (count == 1 significa que só pertence a este artista)
                   List<Uni<Void>> limpezas = albumIds.stream()
                       .map(albumId -> session.createQuery(
                               "select count(a) from Artista a join a.albuns alb where alb.id = :albId", Long.class)
                               .setParameter("albId", albumId)
                               .getSingleResult()
                               .flatMap(count -> {
                                   // Se o count é 1, ele é exclusivo deste artista que será deletado
                                   if (count <= 1) {
                                       // Removemos o vínculo na tabela intermediária manualmente para evitar conflitos de Queue
                                       return session.createNativeQuery("DELETE FROM artista_album WHERE album_id = :albId")
                                               .setParameter("albId", albumId)
                                               .executeUpdate()
                                               .flatMap(r -> Album.deleteById(albumId).replaceWithVoid());
                                   }
                                   return Uni.createFrom().voidItem();
                               })
                       ).toList();

                   // 3. Executa as limpezas e depois deleta o artista por último
                   return Uni.combine().all().unis(limpezas).discardItems()
                           .flatMap(v -> Artista.deleteById(id));
               })
    )
    .onItem().invoke(deletado -> {
        if (deletado) {
            webSocket.broadcast("Remoção completa realizada para o artista ID: " + id);
        }
    });
}
*/

/*
    @WithTransaction
    public Uni<Boolean> deletar(Long id) {
        // 1. Buscamos o artista com fetch join para garantir que os álbuns venham na mesma query
        return Artista.find("select distinct a from Artista a left join fetch a.albuns where a.id = ?1", id)
            .firstResult()
            .onItem().ifNotNull().transformToUni(obj -> {
                // FAÇA EXATAMENTE ASSIM: Fazemos o cast explícito para a IDE parar de reclamar
                Artista artista = (Artista) obj;
                
                // Extraímos a lista para uma variável local tipada
                List<Album> listaDeAlbuns = artista.albuns;
                final List<Album> albunsParaAnalisar = (listaDeAlbuns != null) ? List.copyOf(listaDeAlbuns) : List.of();

                // 2. Deletamos o Artista (O CASCADE do banco limpa a tabela artista_album)
                return Artista.deleteById(id)
                    .onItem().transformToUni(deletado -> {
                        if (deletado && !albunsParaAnalisar.isEmpty()) {
                            
                            // 3. Lógica de limpeza de órfãos: verifica se o álbum ainda tem donos
                            List<Uni<Void>> verificacoes = albunsParaAnalisar.stream()
                                .map(album -> Artista.getSession().flatMap(session -> 
                                    session.createQuery("select count(a) from Artista a join a.albuns alb where alb.id = :albumId", Long.class)
                                        .setParameter("albumId", album.id)
                                        .getSingleResult()
                                        .flatMap(count -> {
                                            // Se count == 0, ninguém mais usa esse álbum. Deletar!
                                            if (count == 0) {
                                                return Album.deleteById(album.id).replaceWithVoid();
                                            }
                                            return Uni.createFrom().voidItem();
                                        })
                                )).toList();

                            return Uni.combine().all().unis(verificacoes).discardItems().replaceWith(true);
                        }
                        return Uni.createFrom().item(deletado);
                    });
            })
            .onItem().ifNull().continueWith(false)
            .onItem().invoke(deletado -> {
                if (deletado) {
                    webSocket.broadcast("Artista e álbuns órfãos removidos com sucesso. ID: " + id);
                }
            });    }
*/
}
