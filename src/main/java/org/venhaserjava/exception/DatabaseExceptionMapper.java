package org.venhaserjava.exception;

import java.util.List;

import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.hibernate.exception.ConstraintViolationException;

// 
//   Mapeador especializado em capturar exceções de integridade do Hibernate/JPA.
//   Intercepta erros de banco de dados (como exclusão de registros com vínculos) 
//   e os transforma em respostas HTTP 409 (Conflict) com mensagens legíveis.
//  
//  
@Provider
public class DatabaseExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    // 
    //  Converte uma falha técnica de restrição de banco em uma resposta amigável.
    //  @param exception Exceção original do Hibernate.
    //  @return Response com status 409 e payload de erro detalhado.
    // 
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Erro 23503 no Postgres é violação de chave estrangeira
        String mensagem = "Não é possível excluir ou alterar este registro pois ele está sendo usado por outros dados.";
        
        return Response.status(Response.Status.CONFLICT) 
                .entity(new ErrorPayload("Erro de Integridade no Banco", List.of(mensagem)))
                .build();
    }
}
