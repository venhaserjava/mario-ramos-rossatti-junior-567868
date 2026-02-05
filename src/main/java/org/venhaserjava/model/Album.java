package org.venhaserjava.model;

import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

/**
 * Entidade que representa um Álbum musical no sistema.
 * <p>
 * Utiliza Hibernate Reactive com Panache para operações de banco de dados assíncronas,
 * garantindo alta performance conforme os requisitos do edital SEPLAG 2026.
 * </p>
 * * @author Mario Ramos Rossatti Junior
 * @see Artista
 */
@Entity
@Table(name = "albuns")
public class Album extends PanacheEntityBase {

    /**
     * Identificador único do álbum. Gerado automaticamente pelo banco de dados (Serial/Identity).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Título do álbum. 
     * Campo obrigatório tanto na validação do Bean quanto na restrição da coluna do banco.
     */
    @Column(name = "titulo", nullable = false)
    @JsonProperty("titulo")
    @NotBlank(message = "O título do álbum é obrigatório")
    public String titulo;

    /**
     * Ano de lançamento do álbum.
     * Possui validação para garantir consistência histórica (mínimo 1900 e máximo 2026).
     */
    @JsonProperty("anoLancamento")
    @Column(name = "ano_lancamento")
    @Min(value = 1900, message = "O ano de lançamento deve ser maior que 1900")
    @Max(value = 2026, message = "O ano de lançamento não pode ser no futuro")
    public Integer anoLancamento;

    /**
     * Lista de artistas vinculados a este álbum.
     * Mapeamento Bidirecional N:N. O ignore evita recursão infinita na serialização JSON.
     */
    @ManyToMany(mappedBy = "albuns")
    @JsonIgnore
    public List<Artista> artistas;

    /**
     * URL da capa armazenada no provedor S3 (MinIO).
     * <p>
     * Nota de Arquitetura: O tamanho foi expandido para 1000 caracteres para suportar 
     * URLs pré-assinadas que contêm tokens de segurança extensos.
     * </p>
     */
    @Column(name = "capa_url", length = 1000)
    @JsonProperty("capaUrl")
    public String capaUrl;

    /**
     * Construtor padrão exigido pela especificação JPA.
     */
    public Album() {
    }
}