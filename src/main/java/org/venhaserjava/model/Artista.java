package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "artistas")
public class Artista extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @NotBlank(message = "O nome do artista é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    public String nome;

    @NotBlank(message = "O tipo (Banda/Solo) é obrigatório")
    public String tipo;
    
    // Adicionamos MERGE para aceitar álbuns existentes cadstrados por outros artistas
    // e REMOVE para ajudar na limpeza controlada
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "artista_album",
        joinColumns = @JoinColumn(name = "artista_id"),
        inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    public List<Album> albuns;
}