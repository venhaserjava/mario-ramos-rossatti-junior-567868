package org.venhaserjava.resource;

import java.util.List;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.venhaserjava.dto.AlbumResponseDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.AlbumService;

@Path("/v1/albuns")
@Produces("application/json")
@Consumes("application/json")
public class AlbumResource {

    @Inject
    AlbumService albumService;

    @GET
    public Uni<List<AlbumResponseDTO>> buscar(
            @QueryParam("titulo") @DefaultValue("") String titulo,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("10") int size) {
        return albumService.buscarPorTitulo(titulo, page, size);
    }

    @POST
    @Path("/artista/{artistaId}")
    @RolesAllowed("ADMIN")
    public Uni<Response> criar(@PathParam("artistaId") Long artistaId, Album album) {
        return albumService.criarComArtista(artistaId, album)
                .map(novoAlbum -> Response.status(Response.Status.CREATED).entity(novoAlbum).build());
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> atualizar(@PathParam("id") Long id, Album album) {
        return albumService.atualizar(id, album)
                .map(atualizado -> Response.ok(atualizado).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }

    @DELETE
    @Path("/{albumId}/artista/{artistaId}")
    @RolesAllowed("ADMIN")
    public Uni<Response> deletar(@PathParam("artistaId") Long artistaId, @PathParam("albumId") Long albumId) {
        return albumService.deletarAlbumComSeguranca(artistaId, albumId)
                .replaceWith(Response.noContent().build());
    }
}