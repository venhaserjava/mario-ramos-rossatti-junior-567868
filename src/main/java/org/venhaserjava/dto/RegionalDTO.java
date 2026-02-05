package org.venhaserjava.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;

/**
 * Data Transfer Object (DTO) para a entidade Regional.
 * <p>
 * Este DTO é utilizado no fluxo de <b>Sincronização de Regionais</b> (Item 04 do Checklist).
 * Ele mapeia os dados recebidos da API externa para a estrutura interna do sistema,
 * garantindo que a integridade dos dados seja validada antes da persistência.
 * </p>
 * * @author Mario Ramos Rossatti Junior
 * @see org.venhaserjava.model.Regional
 */
public class RegionalDTO {

    /**
     * Nome da unidade regional enviado pela API externa.
     * <p>
     * O edital prevê atributos dinâmicos, por isso este campo é central para o 
     * mapeamento de nomes alterados ou novas unidades.
     * </p>
     */
    @NotBlank(message = "O nome da regional não pode estar em branco")
    @Size(min = 2, max = 200, message = "O nome deve conter entre 2 e 200 caracteres")
    public String nome;

    /**
     * Construtor padrão necessário para a desserialização via JSON (Jackson).
     */
    public RegionalDTO() {
    }

    /**
     * Construtor de conveniência para criação rápida de DTOs em testes.
     * * @param nome Nome da regional a ser instanciada.
     */
    public RegionalDTO(String nome) {
        this.nome = nome;
    }
}
