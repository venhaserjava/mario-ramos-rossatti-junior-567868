package org.venhaserjava.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.venhaserjava.dto.AlbumResponseDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.model.Artista;
import org.venhaserjava.websocket.ArtistaWebSocket;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class AlbumService {

    @Inject
    ArtistaWebSocket webSocket; // Injetando o WebSocket para notificações em tempo real

    public Uni<Album> criarComArtista(Long artistaId, Album album) {
            // 1. Usamos uma query com FETCH JOIN para trazer o artista e seus álbuns de uma vez só.
            // Isso evita o LazyInitializationException e facilita a tipagem.
            return Panache.withTransaction(() -> 
                Artista.<Artista>find("from Artista a left join fetch a.albuns where a.id = ?1", artistaId)
                    .firstResult()
                    .onItem().ifNull().failWith(() -> new WebApplicationException("Artista não encontrado", Response.Status.BAD_REQUEST))
                    .onItem().transformToUni((Artista artista) -> {
                        
                        // 2. Vinculamos as duas pontas do relacionamento N:N
                        if (album.artistas == null) album.artistas = new ArrayList<>();
                        album.artistas.add(artista);
                        
                        if (artista.albuns == null) artista.albuns = new ArrayList<>();
                        artista.albuns.add(album);
                        
                        // 3. Persistimos o álbum. O retorno será Uni<Album>
                        return album.<Album>persist();
                    })
            ).onItem().invoke(novoAlbum -> 
                webSocket.broadcast("NOVO_ALBUM: " + novoAlbum.titulo + " (ID: " + novoAlbum.id + ")")
            );
        }
       

    /**
     * PUT: Atualiza título/ano e notifica via WS
     */
    public Uni<Album> atualizar(Long id, Album albumAtualizado) {
        return Panache.withTransaction(() -> 
            Album.<Album>findById(id)
                .onItem().ifNotNull().transformToUni((Album album) -> {
                    album.titulo = albumAtualizado.titulo;
                    album.anoLancamento = albumAtualizado.anoLancamento;
                    return Uni.createFrom().item(album);
                })
        ).onItem().invoke(atualizado -> {
            if (atualizado != null) {
                webSocket.broadcast("ALBUM_ATUALIZADO: " + atualizado.titulo);
            }
        });
    }

  /**
     * DELETE: Remove o álbum e todas as suas associações automaticamente.
     * Agora recebe apenas o albumId, conforme sua sugestão.
     */
    public Uni<Void> deletarAlbum(Long id) {
        return Panache.withTransaction(() -> 
            Album.<Album>findById(id)
                .onItem().ifNotNull().transformToUni((Album album) -> {
                    String titulo = album.titulo;
                    
                    // O Hibernate, devido ao ManyToMany, cuidará de remover 
                    // as linhas na tabela artista_album automaticamente
                    // ao deletarmos a entidade Album.
                    return album.delete().replaceWith(titulo);
                })
        )
        .onItem().ifNotNull().invoke(titulo -> 
            webSocket.broadcast("ALBUM_DELETADO: " + titulo)
        ).replaceWithVoid();
    }
    /**
     * DELETE: Lógica N:N com notificação via WS
     
    public Uni<Void> deletarAlbumComSeguranca(Long artistaId, Long albumId) {
        return Panache.withTransaction(() -> 
            Album.<Album>findById(albumId)
                .onItem().ifNotNull().transformToUni((Album album) -> {
                    String tituloParaLog = album.titulo;
                    return Artista.<Artista>findById(artistaId).onItem().ifNotNull().transformToUni((Artista artista) -> {
                        album.artistas.remove(artista);
                        if (album.artistas.isEmpty()) {
                            return album.delete().replaceWith(tituloParaLog);
                        }
                        return Uni.createFrom().item(tituloParaLog + " (Vínculo removido)");
                    });
                })
        ).onItem().invoke(msg -> 
            webSocket.broadcast("ALBUM_REMOVIDO: " + msg)
        ).replaceWithVoid();
    }
*/

    /**
     * GET2: Busca por título com DTO e paginação (Sem notificação pois é leitura)
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