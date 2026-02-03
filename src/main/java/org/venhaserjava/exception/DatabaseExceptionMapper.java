package org.venhaserjava.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.hibernate.exception.ConstraintViolationException;
import java.util.List;

@Provider
public class DatabaseExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        // Erro 23503 no Postgres é violação de chave estrangeira
        String mensagem = "Não é possível excluir ou alterar este registro pois ele está sendo usado por outros dados.";
        
        return Response.status(Response.Status.CONFLICT) // 409 Conflict
                .entity(new ErrorPayload("Erro de Integridade no Banco", List.of(mensagem)))
                .build();
    }
}
