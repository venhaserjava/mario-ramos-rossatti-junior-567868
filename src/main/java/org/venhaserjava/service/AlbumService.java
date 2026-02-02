package org.venhaserjava.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.venhaserjava.dto.AlbumResponseDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.model.Artista;
import org.venhaserjava.websocket.ArtistaWebSocket;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AlbumService {

    @Inject
    ArtistaWebSocket webSocket;

    public Uni<Album> criarComArtista(Long artistaId, Album album) {
            // Forçamos o withTransaction a entender que retornará um Album
            return Panache.<Album>withTransaction(() -> 
                Artista.<Artista>find("from Artista a left join fetch a.albuns where a.id = ?1", artistaId)
                    .firstResult()
                    .onItem().ifNull().failWith(() -> 
                        new WebApplicationException("Artista não encontrado", Response.Status.BAD_REQUEST)
                    )
                    .onItem().transformToUni((Artista artista) -> {
                        // Inicialização de segurança das listas (evita NullPointerException)
                        if (album.artistas == null) album.artistas = new ArrayList<>();
                        if (artista.albuns == null) artista.albuns = new ArrayList<>();

                        // Vinculação bidirecional
                        album.artistas.add(artista);
                        artista.albuns.add(album);
                        
                        // Explicitamente retornando Uni<Album>
                        return album.<Album>persist();
                    })
            ).onItem().invoke(novoAlbum -> 
                webSocket.broadcast("NOVO_ALBUM: " + novoAlbum.titulo + " (ID: " + novoAlbum.id + ")")
            );
        }
        
    public Uni<Album> atualizar(Long id, Album albumAtualizado) {
        return Panache.withTransaction(() -> 
            Album.<Album>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Álbum não encontrado"))
                .onItem().invoke(album -> {
                    album.titulo = albumAtualizado.titulo;
                    album.anoLancamento = albumAtualizado.anoLancamento;
                })
        ).onItem().invoke(atualizado -> 
            webSocket.broadcast("ALBUM_ATUALIZADO: " + atualizado.titulo)
        );
    }

    @WithTransaction
    public Uni<Void> deletarAlbum(Long id) {
        return Album.getSession().flatMap(session -> 
            // 1. Primeiro, buscamos o título para o broadcast (antes de deletar)
            Album.<Album>findById(id)
                .onItem().ifNull().failWith(() -> new NotFoundException("Álbum não encontrado"))
                .flatMap(album -> {
                    String titulo = album.titulo;

                    // 2. Usamos SQL Nativo para limpar a tabela de ligação primeiro
                    // Isso remove qualquer vínculo deste álbum com artistas
                    return session.createNativeQuery("DELETE FROM artista_album WHERE album_id = :id")
                        .setParameter("id", id)
                        .executeUpdate()
                        .flatMap(v -> 
                            // 3. Agora que a restrição de FK foi limpa no braço, deletamos o álbum
                            session.createNativeQuery("DELETE FROM albuns WHERE id = :id")
                                .setParameter("id", id)
                                .executeUpdate()
                        )
                        .replaceWith(titulo);
                })
        ).invoke(titulo -> 
            webSocket.broadcast("ALBUM_DELETADO: " + titulo)
        ).replaceWithVoid();
    }
        // public Uni<Void> deletarAlbum(Long id) {
    //     return Panache.withTransaction(() -> 
    //         // 1. Buscamos o álbum e CARREGAMOS os artistas (join fetch)
    //         Album.<Album>find("from Album a left join fetch a.artistas where a.id = ?1", id)
    //             .firstResult()
    //             .onItem().ifNull().failWith(() -> new NotFoundException("Álbum não encontrado"))
    //             .onItem().transformToUni(album -> {
    //                 String titulo = album.titulo;
                    
    //                 // 2. LIMPEZA MANUAL DA ASSOCIAÇÃO
    //                 // Isso remove a linha na tabela 'artista_album'
    //                 album.artistas.clear(); 
                    
    //                 // 3. AGORA SIM, DELETAMOS O ÁLBUM
    //                 // O flush do Hibernate vai disparar o DELETE na tabela de ligação primeiro
    //                 return album.delete().replaceWith(titulo);
    //             })
    //     ).onItem().invoke(titulo -> 
    //         webSocket.broadcast("ALBUM_DELETADO: " + titulo)
    //     ).replaceWithVoid();
    // }


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