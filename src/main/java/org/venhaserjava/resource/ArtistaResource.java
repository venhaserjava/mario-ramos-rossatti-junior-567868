package org.venhaserjava.resource;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.venhaserjava.model.Artista;
import java.util.List;

@Path("/artistas")
public class ArtistaResource {

    @GET
    public Uni<List<Artista>> listarTodos() {
        return Artista.listAll();
    }
}
