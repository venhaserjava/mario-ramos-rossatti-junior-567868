package org.venhaserjava.service;


import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.venhaserjava.model.Artista;
import java.util.List;

@ApplicationScoped
public class ArtistaService {

    public Uni<List<Artista>> listarTodos() {
        // Adicionamos 'distinct' para evitar duplicatas causadas pelo join fetch
        return Artista.list("select distinct a from Artista a left join fetch a.albuns");
    }

    @WithTransaction
    public Uni<Artista> salvar(Artista artista) {
        return Artista.persist(artista).replaceWith(artista);
    }

    public Uni<List<Artista>> listarPaginado(int pageIndex, int pageSize) {
        // O Panache permite encadear .page() para gerenciar o LIMIT e OFFSET automaticamente
        return Artista.find("select distinct a from Artista a left join fetch a.albuns")
                .page(pageIndex, pageSize)
                .list();
    }

     
}