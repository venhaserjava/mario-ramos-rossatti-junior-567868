package org.venhaserjava.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Vertx;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.venhaserjava.dto.FileUploadDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.S3Service;

@Path("/capas")
public class CapaResource {

    @Inject
    S3Service s3Service;

    @Inject
    Vertx vertx; // Injetamos o Vertx para gerenciar o contexto

    @POST
    @Path("/upload/{albumId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Album> uploadEVincular(@PathParam("albumId") Long albumId, FileUploadDTO formData) {
        if (formData.file == null) {
            throw new BadRequestException("Arquivo não enviado.");
        }

        // 1. Capturamos o contexto do Vert.x da thread atual (Event Loop)
        var context = vertx.getOrCreateContext();

        return s3Service.uploadCapa(formData.file.filePath(), formData.file.contentType())
            .onItem().transformToUni(url -> 
                // 2. Forçamos a execução de volta no contexto capturado
                Uni.createFrom().<Album>emitter(emitter -> 
                    context.runOnContext(v -> 
                        Panache.withTransaction(() -> 
                            Album.<Album>findById(albumId)
                                .onItem().ifNotNull().transformToUni(album -> {
                                    album.capaUrl = url;
                                    return album.persist().replaceWith(album);
                                })
                                .onItem().ifNull().failWith(new NotFoundException("Álbum não encontrado"))
                        ).subscribe().with(emitter::complete, emitter::fail)
                    )
                )
            );
    }
}

/*
package org.venhaserjava.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.venhaserjava.dto.FileUploadDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.S3Service;

@Path("/capas")
public class CapaResource {

    @Inject
    S3Service s3Service;

    @POST
    @Path("/upload/{albumId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Album> uploadEVincular(@PathParam("albumId") Long albumId, FileUploadDTO formData) {
        if (formData.file == null) {
            throw new BadRequestException("Arquivo não enviado.");
        }

        return s3Service.uploadCapa(formData.file.filePath(), formData.file.contentType())
            // FORÇAMOS a volta para o contexto do Quarkus/Vert.x aqui
            .emitOn(Infrastructure.getDefaultExecutor()) 
            .onItem().transformToUni(url -> 
                Panache.withTransaction(() -> 
                    Album.<Album>findById(albumId)
                        .onItem().ifNotNull().transformToUni(album -> {
                            album.capaUrl = url;
                            // Usamos persist para garantir a gravação
                            return album.persist().replaceWith(album);
                        })
                        .onItem().ifNull().failWith(new NotFoundException("Álbum não encontrado com ID: " + albumId))
                )
            );
    }
}
*/


/*
package org.venhaserjava.resource;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.venhaserjava.dto.FileUploadDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.S3Service;

@Path("/capas")
public class CapaResource {

    @Inject
    S3Service s3Service;

    @POST
    @Path("/upload/{albumId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Album> uploadEVincular(@PathParam("albumId") Long albumId, FileUploadDTO formData) {
        if (formData.file == null) {
            throw new BadRequestException("Arquivo não enviado.");
        }

        // 1. Primeiro fazemos o upload (Thread AWS)
        return s3Service.uploadCapa(formData.file.filePath(), formData.file.contentType())
            .onItem().transformToUni(url -> 
                // 2. Usamos o Panache.withSession para reestabelecer o contexto do Hibernate
                Panache.withSession(() -> 
                    Album.<Album>findById(albumId)
                        .onItem().ifNotNull().transformToUni(album -> {
                            album.capaUrl = url;
                            return album.persist().replaceWith(album);
                        })
                        .onItem().ifNull().failWith(new NotFoundException("Álbum não encontrado com ID: " + albumId))
                )
            );
    }
}
*/


/*
package org.venhaserjava.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.venhaserjava.dto.FileUploadDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.S3Service;

@Path("/capas")
public class CapaResource {

    @Inject
    S3Service s3Service;

    @POST
    @Path("/upload/{albumId}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Album> uploadEVincular(@PathParam("albumId") Long albumId, FileUploadDTO formData) {
        if (formData.file == null) {
            throw new BadRequestException("Arquivo não enviado.");
        }

        return s3Service.uploadCapa(formData.file.filePath(), formData.file.contentType())
                .onItem().transformToUni(url ->
                        // Adicionamos a tipagem explícita <Album> no findById
                        Album.<Album>findById(albumId)
                                .onItem().ifNotNull().transformToUni(album -> {
                                    album.capaUrl = url;
                                    // O persist() retorna Uni<Void> ou Uni<Entity>,
                                    // garantimos que retorne o album atualizado
                                    return album.persist().replaceWith(album);
                                })
                                .onItem().ifNull().failWith(new NotFoundException("Álbum não encontrado com ID: " + albumId))
                );
    }
}
*/    
/*
package org.venhaserjava.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.venhaserjava.dto.FileUploadDTO;
import org.venhaserjava.service.S3Service;

@Path("/capas")
public class CapaResource {

    @Inject
    S3Service s3Service;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> upload(FileUploadDTO formData) {
        if (formData.file == null) {
            throw new BadRequestException("Arquivo não enviado.");
        }
        
        // Chamamos o seu serviço passando o Path temporário e o tipo do arquivo
        return s3Service.uploadCapa(formData.file.filePath(), formData.file.contentType());
    }
}
*/