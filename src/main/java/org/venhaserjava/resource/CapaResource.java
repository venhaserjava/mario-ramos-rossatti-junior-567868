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
