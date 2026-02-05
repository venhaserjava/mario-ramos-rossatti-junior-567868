package org.venhaserjava.exception;

import java.util.List;

/**
 * Representa a estrutura padronizada de resposta para erros da API.
 * [SÊNIOR] Centraliza a comunicação de falhas, garantindo que o cliente 
 * receba sempre o mesmo formato de payload, facilitando o tratamento no frontend.
 *
 */
public class ErrorPayload {
    /**
     * Resumo amigável do erro ocorrido.
     */
    public String mensagem;
    
    /**
     * Lista detalhada de inconsistências (ex: campos inválidos ou causas técnicas).
     */
    public List<String> detalhes;

    public ErrorPayload(String mensagem, List<String> detalhes) {
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }
}
