package org.venhaserjava.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;

/**
 * Entidade que representa uma Unidade Regional no sistema.
 * <p>
 * Esta classe é fundamental para o requisito de <b>Sincronização de Regionais</b> 
 * (Item 04 do Checklist), permitindo a ativação ou inativação de unidades 
 * via integração com APIs externas.
 * </p>
 * * @author Mario Ramos Rossatti Junior
 */
@Entity
@Table(name = "regionais")
public class Regional extends PanacheEntityBase {

    /**
     * Identificador único da Regional.
     * Gerado via IDENTITY para garantir a integridade no banco PostgreSQL conteinerizado.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    /**
     * Nome descritivo da Regional.
     * Possui restrição de tamanho de 200 caracteres conforme o schema do banco de dados.
     */
    @NotBlank(message = "O nome da Regional é obrigatório")
    @Size(min = 2, max = 200, message = "O nome deve ter entre 2 e 200 caracteres")
    @Column(length = 200, nullable = false)
    public String nome;

    /**
     * Define se a Regional está operacional no sistema.
     * <p>
     * Utilizado pela lógica de sincronização para realizar o 'soft delete' ou 
     * reativação de unidades sem perda de histórico transacional.
     * </p>
     */
    public Boolean ativo = true;

    /**
     * Construtor padrão para inicialização via Hibernate Reactive.
     */
    public Regional() {
    }
}
