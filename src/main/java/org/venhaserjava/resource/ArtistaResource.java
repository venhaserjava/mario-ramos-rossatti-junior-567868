package org.venhaserjava.resource;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.venhaserjava.model.Artista;
import java.util.List;

@Path("/artistas")
@Produces("application/json")
@Consumes("application/json")
public class ArtistaResource {

    @GET
    public Uni<List<Artista>> listarTodos() {
        return Artista.listAll();
    }

    @POST
    @WithTransaction
    public Uni<Response> criar(Artista artista) {
        // .persist() retorna um Uni<Void>. Usamos .replaceWith para retornar o objeto salvo.
        return Artista.persist(artista)
                .replaceWith(() -> Response.status(Response.Status.CREATED).entity(artista).build());
    }
}