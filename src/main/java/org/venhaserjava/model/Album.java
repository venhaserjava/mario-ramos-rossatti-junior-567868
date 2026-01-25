package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "albuns")
public class Album extends PanacheEntity {

    public String titulo;

    @ManyToMany(mappedBy = "albuns")
    @JsonIgnore // Evita recursão infinita no JSON
    public List<Artista> artistas;
}