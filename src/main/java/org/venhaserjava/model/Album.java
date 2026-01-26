package org.venhaserjava.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "albuns")
public class Album extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String titulo; // Nome correto conforme V1.0.0

    @Column(name = "ano_lancamento")
    public Integer anoLancamento;

    @ManyToMany(mappedBy = "albuns")
    @JsonIgnore
    public List<Artista> artistas;
}