package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "artistas")
public class Artista extends PanacheEntity {
    public String nome;
    public String tipo;
}