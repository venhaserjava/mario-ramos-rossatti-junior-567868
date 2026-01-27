package org.venhaserjava.service;


import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import org.venhaserjava.model.Album;
import org.venhaserjava.model.Artista;
import java.util.List;

@ApplicationScoped
public class ArtistaService {

    public Uni<List<Artista>> listarComFiltros(int page, int size, String nome, String tipo, String order) {
        // Query otimizada para carregar álbuns sem múltiplas consultas
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
        // Persist é ideal para novos registros
        return Artista.persist(artista).replaceWith(artista);
    }
/* 
    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.getSession().flatMap(session -> 
            Artista.<Artista>findById(id)
                .onItem().ifNotNull().transformToUni(existente -> {
                    // Garantimos que o ID da URL seja o ID do objeto
                    artista.id = id;
                    
                    // O merge sincroniza o Artista E os Albuns da lista (devido ao Cascade)
                    // resolvendo tanto o problema de 'detached entity' quanto de campos nulos
                    return session.merge(artista);
                })
        );
    }
*/    
/*
    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.getSession().flatMap(session -> 
            Artista.<Artista>findById(id)
                .onItem().ifNotNull().transformToUni(existente -> {
                    // Garantimos que o ID da URL seja o ID do objeto
                    artista.id = id;
                    
                    // Sincronização manual do lado inverso (Boa prática para evitar o erro de null)
                    if (artista.albuns != null) {
                        artista.albuns.forEach(album -> {
                            if (album.artistas == null) {
                                album.artistas = new java.util.ArrayList<>();
                            }
                            if (!album.artistas.contains(artista)) {
                                album.artistas.add(artista);
                            }
                        });
                    }
                    
                    return session.merge(artista);
                })
        );
    }
*/
/*
    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.<Artista>findById(id)
            .onItem().ifNotNull().transformToUni(existente -> {
                // 1. Atualiza campos básicos do Artista
                existente.nome = artista.nome;
                existente.tipo = artista.tipo;

                // 2. Tratamento manual da coleção para garantir a persistência
                if (artista.albuns != null) {
                    // Limpamos a lista atual e re-adicionamos para forçar o Hibernate a rastrear
                    return Artista.getSession().flatMap(session -> {
                        // Usamos fetch para garantir que os álbuns novos sejam "anexados" corretamente
                        List<Uni<Album>> albumUnis = artista.albuns.stream()
                            .map(a -> {
                                if (a.id == null) {
                                    // Se for novo, garante que ele seja persistido/gerenciado
                                    return session.merge(a);
                                } else {
                                    // Se já existe, apenas mescla o estado
                                    return session.merge(a);
                                }
                            }).toList();

                        return Uni.combine().all().unis(albumUnis).with(List.class, mergedAlbuns -> {
                            existente.albuns = mergedAlbuns;
                            return existente;
                        });
                    });
                }
                return Uni.createFrom().item(existente);
            })
            .onItem().ifNotNull().transformToUni(e -> e.persist());
    }
*/
    @SuppressWarnings("removal")
    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.<Artista>findById(id)
            .onItem().ifNotNull().transformToUni(existente -> {
                // 1. Atualiza campos básicos
                existente.nome = artista.nome;
                existente.tipo = artista.tipo;

                if (artista.albuns != null) {
                    return Artista.getSession().flatMap(session -> {
                        List<Uni<Album>> albumUnis = artista.albuns.stream()
                            .map(session::merge) // Simplificado: merge serve para novos e existentes
                            .toList();

                        // A MUDANÇA ESTÁ AQUI: Usamos cast explícito ou tipagem no lambda
                        return Uni.combine().all().unis(albumUnis)
                            .combinedWith(list -> {
                                // Convertemos a lista genérica do Mutiny para List<Album>
                                @SuppressWarnings("unchecked")
                                List<Album> mergedAlbuns = (List<Album>) list;
                                existente.albuns = mergedAlbuns;
                                return existente;
                            });
                    });
                }
                return Uni.createFrom().item(existente);
            });
            // Removido o .persist() final, pois 'existente' já é gerenciado e está em @WithTransaction
    }


}
