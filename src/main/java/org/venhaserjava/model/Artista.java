package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "artistas")
public class Artista extends PanacheEntity {

    public String nome;
    public String tipo;

    @ManyToMany(fetch = FetchType.EAGER) // Eager para facilitar o nosso GET inicial
    @JoinTable(
            name = "artista_album",
            joinColumns = @JoinColumn(name = "artista_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    public List<Album> albuns = new ArrayList<>();
}