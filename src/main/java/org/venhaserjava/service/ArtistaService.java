package org.venhaserjava.service;


import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.venhaserjava.model.Artista;
import java.util.List;

@ApplicationScoped
public class ArtistaService {

    public Uni<List<Artista>> listarComFiltros(int page, int size, String nome, String tipo, String order) {
        // Query otimizada para carregar álbuns sem múltiplas consultas
        StringBuilder query = new StringBuilder("select distinct a from Artista a left join fetch a.albuns where 1=1");
        Parameters params = new Parameters();

        if (nome != null && !nome.isBlank()) {
            query.append(" and lower(a.nome) like lower(:nome)");
            params.and("nome", "%" + nome + "%");
        }

        if (tipo != null && !tipo.isBlank()) {
            query.append(" and a.tipo = :tipo");
            params.and("tipo", tipo);
        }

        Sort sort = "desc".equalsIgnoreCase(order) ? Sort.descending("nome") : Sort.ascending("nome");

        return Artista.find(query.toString(), sort, params)
                .page(page, size)
                .list();
    }

    @WithTransaction
    public Uni<Artista> salvar(Artista artista) {
        // Persist é ideal para novos registros
        return Artista.persist(artista).replaceWith(artista);
    }

    @WithTransaction
    public Uni<Artista> atualizar(Long id, Artista artista) {
        return Artista.getSession().flatMap(session -> 
            Artista.<Artista>findById(id)
                .onItem().ifNotNull().transformToUni(existente -> {
                    // Garantimos que o ID da URL seja o ID do objeto
                    artista.id = id;
                    
                    // O merge sincroniza o Artista E os Albuns da lista (devido ao Cascade)
                    // resolvendo tanto o problema de 'detached entity' quanto de campos nulos
                    return session.merge(artista);
                })
        );
    }
}
