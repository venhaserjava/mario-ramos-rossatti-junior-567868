package org.venhaserjava.resource;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.venhaserjava.model.Artista;
import org.venhaserjava.service.ArtistaService;
import java.util.List;

@Path("/v1/artistas") // Adicionado o prefixo /v1 conforme edital
@Produces("application/json")
@Consumes("application/json")
public class ArtistaResource {
    // ... restante do código permanece igual
@Inject
    ArtistaService artistaService;

    @GET
    public Uni<List<Artista>> listar(
            @QueryParam("page") @DefaultValue("0") int page, 
            @QueryParam("size") @DefaultValue("10") int size) {
        return artistaService.listarPaginado(page, size);
    }

    @RolesAllowed("ADMIN")
    @POST
    public Uni<Response> criar(Artista artista) {
        return artistaService.salvar(artista)
                .map(novoArtista -> Response.status(Response.Status.CREATED).entity(novoArtista).build());
    }
    
}