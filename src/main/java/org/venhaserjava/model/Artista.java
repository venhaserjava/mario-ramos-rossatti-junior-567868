package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "artistas")
public class Artista extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String nome;
    public String tipo;
    
    @ManyToMany
    @JoinTable(
        name = "artista_album",
        joinColumns = @JoinColumn(name = "artista_id"),
        inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    public List<Album> albuns;
}