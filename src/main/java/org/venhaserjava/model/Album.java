package org.venhaserjava.model;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "albuns")
public class Album extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    // No arquivo Album.java
    @Column(name = "titulo",nullable = false) // Boa prática Sênior: reforça a regra do banco no Java
    @JsonProperty("titulo")
    @NotBlank(message = "O título do álbum é obrigatório")
    public String titulo; // Nome correto conforme V1.0.0

    @JsonProperty("anoLancamento")
    @Column(name = "ano_lancamento")
    @Min(value = 1900, message = "O ano de lançamento deve ser maior que 1900")
    @Max(value = 2026, message = "O ano de lançamento não pode ser no futuro")
    public Integer anoLancamento;

    @ManyToMany(mappedBy = "albuns")
    @JsonIgnore
    public List<Artista> artistas;

    @Column(name = "capa_url")
    @JsonProperty("capaUrl")
    public String capaUrl;

}