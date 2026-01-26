package org.venhaserjava.resource;


import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.venhaserjava.model.Artista;
import org.venhaserjava.service.ArtistaService;
import java.util.List;

@Path("/v1/artistas")
@Produces("application/json")
@Consumes("application/json")
public class ArtistaResource {

    @Inject
    ArtistaService artistaService;

    @GET
    public Uni<List<Artista>> listar(
            @QueryParam("page") @DefaultValue("0") int page, 
            @QueryParam("size") @DefaultValue("10") int size,
            @QueryParam("nome") String nome,
            @QueryParam("tipo") String tipo,
            @QueryParam("order") @DefaultValue("asc") String order) {
        return artistaService.listarComFiltros(page, size, nome, tipo, order);
    }

    @RolesAllowed("ADMIN")
    @POST
    public Uni<Response> criar(Artista artista) {
        return artistaService.salvar(artista)
                .map(novoArtista -> Response.status(Response.Status.CREATED).entity(novoArtista).build());
    }

    @RolesAllowed("ADMIN")
    @PUT
    @Path("/{id}")
    public Uni<Response> atualizar(@PathParam("id") Long id, Artista artista) {
        return artistaService.atualizar(id, artista)
                .map(atualizado -> Response.ok(atualizado).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }
}
