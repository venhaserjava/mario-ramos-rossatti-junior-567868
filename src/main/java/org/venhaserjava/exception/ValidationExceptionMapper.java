package org.venhaserjava.exception;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

// 
//   Mapeador responsável por capturar falhas de validação de Bean (Jakarta Validation).
//   Coleta todas as violações de campos (ex: nome vazio, ano inválido) 
//   e retorna uma resposta 400 (Bad Request) com a lista de mensagens configuradas nas entidades.
//  
//  
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    //
    //  Transforma as violações de restrição em uma lista de strings para o payload de erro.
    //  @param exception Conjunto de violações detectadas pelo Validator.
    //  @return Response com status 400 e detalhes das validações que falharam.
    // 
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        List<String> erros = exception.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorPayload("Dados inválidos enviados na requisição", erros))
                .build();
    }
}

