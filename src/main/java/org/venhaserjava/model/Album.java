package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "albuns")
public class Album extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id; // Corrigido de id;; para id;

    public String titulo;

    @ManyToMany(mappedBy = "albuns")
    @JsonIgnore // Evita recursão infinita no JSON
    public List<Artista> artistas;
}