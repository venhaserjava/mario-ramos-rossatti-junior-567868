package org.venhaserjava.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.venhaserjava.service.RegionalService;

@Path("/v1/regionais")
@Produces(MediaType.APPLICATION_JSON)
public class RegionalResource {

    @Inject
    RegionalService regionalService;

    // 
    // Gerencia as operações relacionadas às Unidades Regionais.
    // Permite o acionamento manual do processo de sincronização de dados externos.
    // 
    @Operation(summary = "Sincronização de Regionais", description = "Retorna uma lista Regionais")
    @APIResponse(responseCode = "200", description = "Sincronização concluída com sucesso")    
    @POST
    @Path("/sync")
    public Uni<Response> dispararSincronizacao() {
        return regionalService.sincronizar()
                .map(v -> Response.ok("{\"message\": \"Sincronização concluída com sucesso\"}").build())
                .onFailure().recoverWithItem(e -> 
                    Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                            .entity("{\"error\": \"" + e.getMessage() + "\"}").build()
                );
    }
}
