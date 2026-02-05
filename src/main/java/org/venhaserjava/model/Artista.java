package org.venhaserjava.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Entidade que representa um Artista no ecossistema musical.
 * <p>
 * Esta classe utiliza o padrão Active Record via PanacheEntityBase para simplificar
 * as operações de persistência reativa, atendendo aos requisitos de Clean Code
 * e performance do edital SEPLAG-MT.
 * </p>
 * * @author Mario Ramos Rossatti Junior
 * @see Album
 */
@Entity
@Table(name = "artistas")
public class Artista extends PanacheEntityBase {

    /**
     * Identificador único do artista.
     * Gerado via estratégia IDENTITY para compatibilidade total com o PostgreSQL 15.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Nome completo do artista ou nome da banda.
     * Possui validação de tamanho (2 a 100 caracteres) conforme regras de negócio.
     */
    @NotBlank(message = "O nome do artista é obrigatório")
    @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
    public String nome;

    /**
     * Categoria do artista (Ex: Solo, Banda, Dupla).
     * Campo obrigatório para filtros de busca e relatórios.
     */
    @NotBlank(message = "O tipo (Banda/Solo) é obrigatório")
    public String tipo;
    
    /**
     * Relacionamento Muitos-para-Muitos com a entidade Album.
     * <p>
     * <b>Estratégia de Cascata:</b> Utiliza PERSIST e MERGE para permitir que um álbum 
     * existente seja vinculado a novos artistas sem duplicação de dados, 
     * cumprindo o requisito de limpeza de órfãos e integridade referencial.
     * </p>
     */
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "artista_album",
        joinColumns = @JoinColumn(name = "artista_id"),
        inverseJoinColumns = @JoinColumn(name = "album_id")
    )
    public List<Album> albuns;

    /**
     * Construtor padrão para o framework Hibernate.
     */
    public Artista() {
    }
}
