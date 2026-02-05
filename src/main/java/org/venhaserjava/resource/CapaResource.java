package org.venhaserjava.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.venhaserjava.dto.FileUploadDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.S3Service;
import org.venhaserjava.websocket.ArtistaWebSocket;

/**
 * Resource responsável pela gestão de mídias e capas dos álbuns.
 * Gerencia o ciclo de vida de arquivos binários integrados ao storage S3.
 */
@Path("/capas")
public class CapaResource {

    @Inject
    S3Service s3Service;

    @Inject
    ArtistaWebSocket webSocket; // Injeção necessária para o broadcast

    @Inject
    Vertx vertx;

    /**
     * Realiza o upload da imagem para o S3 e vincula a URL gerada ao álbum correspondente.
     * Ao final do processo, dispara uma notificação via WebSocket para os clientes conectados.
     *
     * @param albumId ID do álbum a ser atualizado.
     * @param formData DTO contendo o arquivo binário e metadados.
     * @return Uni contendo o álbum atualizado com a nova URL da capa.
     */
    @Operation(summary = "Atualiza a capa do álbum", description = "Faz upload para S3 e notifica via WebSocket.")
    @APIResponse(responseCode = "200", description = "Capa atualizada e notificação enviada")   
    @POST
    @Path("/upload/{albumId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Album> uploadEVincular(
        @Parameter(description = "Id do Album que receberá uma capa.")
        @PathParam("albumId") Long albumId, 
        FileUploadDTO formData
    ) {
        if (formData.file == null) {
            throw new BadRequestException("Arquivo não enviado.");
        }

        var context = vertx.getOrCreateContext();

        return s3Service.uploadCapa(formData.file.filePath(), formData.file.contentType())
            .onItem().transformToUni(url -> 
                Uni.createFrom().<Album>emitter(emitter -> 
                    context.runOnContext(v -> 
                        Panache.withTransaction(() -> 
                            Album.<Album>findById(albumId)
                                .onItem().ifNotNull().transformToUni(album -> {
                                    album.capaUrl = url;
                                    return album.persist().replaceWith(album);
                                })
                                .onItem().ifNull().failWith(new NotFoundException("Álbum não encontrado"))
                        ).subscribe().with(
                            album -> {
                                // [SÊNIOR] Notificamos o WebSocket após o sucesso da transação
                                webSocket.broadcast("Capa atualizada para o álbum: " + album.titulo);
                                emitter.complete(album);
                            }, 
                            emitter::fail
                        )
                    )
                )
            );
    }
}
