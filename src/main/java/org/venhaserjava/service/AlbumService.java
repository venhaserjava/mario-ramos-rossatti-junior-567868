package org.venhaserjava.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.venhaserjava.dto.AlbumResponseDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.model.Artista;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AlbumService {

    /**
     * POST: Cria um álbum vinculado a um artista obrigatório
     */
    public Uni<Album> criarComArtista(Long artistaId, Album album) {
        return Panache.withTransaction(() -> 
            Artista.<Artista>findById(artistaId)
                .onItem().ifNull().failWith(() -> new WebApplicationException("Artista não encontrado", Response.Status.BAD_REQUEST))
                .onItem().ifNotNull().transformToUni((Artista artista) -> {
                    if (album.artistas == null) album.artistas = new ArrayList<>();
                    album.artistas.add(artista);
                    
                    if (artista.albuns == null) artista.albuns = new ArrayList<>();
                    artista.albuns.add(album);
                    
                    return album.<Album>persist();
                })
        );
    }

    /**
     * PUT: Atualiza apenas título e ano
     */
    public Uni<Album> atualizar(Long id, Album albumAtualizado) {
        return Panache.withTransaction(() -> 
            Album.<Album>findById(id)
                .onItem().ifNotNull().transformToUni((Album album) -> {
                    album.titulo = albumAtualizado.titulo;
                    album.anoLancamento = albumAtualizado.anoLancamento;
                    // No Hibernate Reactive, alterações em objetos persistidos 
                    // dentro de uma transação são sincronizadas automaticamente no flush
                    return Uni.createFrom().item(album);
                })
        );
    }

    /**
     * DELETE: Lógica N:N para evitar álbuns órfãos
     */
    public Uni<Void> deletarAlbumComSeguranca(Long artistaId, Long albumId) {
        return Panache.withTransaction(() -> 
            Album.<Album>findById(albumId)
                .onItem().ifNotNull().transformToUni((Album album) -> 
                    Artista.<Artista>findById(artistaId).onItem().ifNotNull().transformToUni((Artista artista) -> {
                        album.artistas.remove(artista);
                        if (album.artistas.isEmpty()) {
                            return album.delete();
                        }
                        return Uni.createFrom().voidItem();
                    })
                ).replaceWithVoid()
        );
    }

    /**
     * GET2: Busca por título com DTO e paginação
     */
    public Uni<List<AlbumResponseDTO>> buscarPorTitulo(String titulo, int page, int size) {
        String query = "FROM Album a LEFT JOIN FETCH a.artistas WHERE a.titulo ILIKE ?1 ORDER BY a.titulo ASC";
        String searchPattern = "%" + titulo + "%";
        
        return Album.<Album>find(query, searchPattern)
                .page(page, size)
                .list()
                .map(list -> list.stream()
                    .map(AlbumResponseDTO::new)
                    .collect(Collectors.toList()));
    }
}