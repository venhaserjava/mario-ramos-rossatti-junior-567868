package org.venhaserjava.exception;

import java.util.List;

public class ErrorPayload {
    public String mensagem;
    public List<String> detalhes;

    public ErrorPayload(String mensagem, List<String> detalhes) {
        this.mensagem = mensagem;
        this.detalhes = detalhes;
    }
}

