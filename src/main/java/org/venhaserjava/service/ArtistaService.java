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
}
