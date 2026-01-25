package org.venhaserjava.resource;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.venhaserjava.model.Artista;
import org.venhaserjava.service.ArtistaService;
import java.util.List;

@Path("/artistas")
@Produces("application/json")
@Consumes("application/json")
public class ArtistaResource {

    @Inject
    ArtistaService artistaService;

    @GET
    public Uni<List<Artista>> listar() {
        return artistaService.listarTodos();
    }

    @RolesAllowed("ADMIN")
    @POST
    public Uni<Response> criar(Artista artista) {
        return artistaService.salvar(artista)
                .map(novoArtista -> Response.status(Response.Status.CREATED).entity(novoArtista).build());
    }
}