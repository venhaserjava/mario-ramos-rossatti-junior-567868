package org.venhaserjava.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;

@Entity
@Table(name = "albuns")
public class Album extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    // No arquivo Album.java
    @Column(name = "titulo",nullable = false) // Boa prática Sênior: reforça a regra do banco no Java
    @JsonProperty("titulo")
    public String titulo; // Nome correto conforme V1.0.0

    @JsonProperty("anoLancamento")
    @Column(name = "ano_lancamento")
    public Integer anoLancamento;

    @ManyToMany(mappedBy = "albuns")
    @JsonIgnore
    public List<Artista> artistas;

    @Column(name = "capa_url")
    @JsonProperty("capaUrl")
    public String capaUrl;

}