package org.venhaserjava.service;


import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.venhaserjava.model.Artista;
import java.util.List;

@ApplicationScoped
public class ArtistaService {

    public Uni<List<Artista>> listarTodos() {
        return Artista.listAll();
    }

    @WithTransaction
    public Uni<Artista> salvar(Artista artista) {
        // Aqui entrarão as regras de negócio no futuro
        return Artista.persist(artista).replaceWith(artista);
    }
}