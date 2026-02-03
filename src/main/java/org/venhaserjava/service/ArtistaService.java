package org.venhaserjava.service;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.venhaserjava.model.Artista;
import org.venhaserjava.websocket.ArtistaWebSocket;

import java.util.List;

@ApplicationScoped
public class ArtistaService {

    @Inject
    ArtistaWebSocket webSocket;

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


    // @WithTransaction
    // public Uni<Artista> salvar(Artista artista) {
    //     return artista.<Artista>persist()
    //             .invoke(novo -> webSocket.broadcast("NOVO_ARTISTA: " + novo.nome + " (ID: " + novo.id + ")"));
    // }
    
    @WithTransaction
    public Uni<Artista> salvar(Artista artista) {
        return Artista.getSession().flatMap(session -> {
            if (artista.id == null) {
                // Se o artista é novo, usamos merge para que o Hibernate 
                // resolva as entidades 'detached' (álbuns com ID) na lista
                return session.merge(artista);
            } else {
                return session.merge(artista);
            }
        }).invoke(novo -> {
            if (novo != null) {
                webSocket.broadcast("ARTISTA_CRIADO: " + novo.nome);
            }
        });
    }



    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.getSession().flatMap(session -> 
            Artista.<Artista>findById(id)
                .onItem().ifNotNull().transformToUni(entity -> {
                    // Atualizamos os dados básicos do artista
                    entity.nome = artista.nome;
                    entity.tipo = artista.tipo;
                    
                    // Atualizamos a referência da lista de álbuns
                    entity.albuns = artista.albuns;

                    // Em vez de entity.persist(), usamos session.merge(entity)
                    // O merge é a operação correta para entidades "Detached" (que vêm do JSON)
                    return session.merge(entity);
                })
        )
        .onItem().invoke(atualizado -> {
            if (atualizado != null) {
                webSocket.broadcast("ARTISTA_ATUALIZADO: " + atualizado.nome);
            }
        });
    }

@WithTransaction
public Uni<Boolean> deletar(Long id) {
    return Artista.getSession().flatMap(session -> 
        // 1. Buscamos os IDs dos álbuns que pertencem APENAS a este artista
        session.createNativeQuery(
            "SELECT album_id FROM artista_album WHERE artista_id = :id " +
            "AND album_id NOT IN (SELECT album_id FROM artista_album WHERE artista_id <> :id)", Long.class)
        .setParameter("id", id)
        .getResultList()
        .flatMap(idsOrfaos -> {
            // 2. PASSO CRUCIAL: Se houver órfãos, limpamos a tabela de ligação para esses álbuns primeiro
            // Isso remove o "vínculo pendurado" que causa o erro 23503
            Uni<Integer> limparLigacoesOrfaos = idsOrfaos.isEmpty() ? 
                Uni.createFrom().item(0) :
                session.createNativeQuery("DELETE FROM artista_album WHERE album_id IN (:ids)")
                    .setParameter("ids", idsOrfaos)
                    .executeUpdate();

            return limparLigacoesOrfaos.flatMap(v -> 
                // 3. Deletamos o Artista (isso limpa o vínculo do artista na tabela de ligação)
                Artista.deleteById(id).flatMap(deletado -> {
                    if (deletado && !idsOrfaos.isEmpty()) {
                        // 4. Agora os álbuns estão totalmente livres para serem apagados
                        return session.createNativeQuery("DELETE FROM albuns WHERE id IN (:ids)")
                                .setParameter("ids", idsOrfaos)
                                .executeUpdate()
                                .replaceWith(true);
                    }
                    return Uni.createFrom().item(deletado);
                })
            );
        })
    ).invoke(deletado -> {
        if (deletado) {
            webSocket.broadcast("ARTISTA_REMOVIDO: ID " + id);
        }
    });
}

    /*
    @WithTransaction
    public Uni<Boolean> deletar(Long id) {
        return Artista.getSession().flatMap(session -> 
            session.createNativeQuery(
                "SELECT album_id FROM artista_album WHERE artista_id = :id " +
                "AND album_id NOT IN (SELECT album_id FROM artista_album WHERE artista_id <> :id)", Long.class)
            .setParameter("id", id)
            .getResultList()
            .flatMap(idsOrfaos -> 
                Artista.deleteById(id)
                    .flatMap(deletado -> {
                        if (deletado && !idsOrfaos.isEmpty()) {
                            return session.createNativeQuery("DELETE FROM albuns WHERE id IN (:ids)")
                                    .setParameter("ids", idsOrfaos)
                                    .executeUpdate()
                                    .replaceWith(true);
                        }
                        return Uni.createFrom().item(deletado);
                    })
            )
        ).invoke(deletado -> {
            if (deletado) {
                webSocket.broadcast("ARTISTA_REMOVIDO: ID " + id);
            }
        });
    }
    */    
}


