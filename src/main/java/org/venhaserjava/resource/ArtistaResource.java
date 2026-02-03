package org.venhaserjava.resource;


import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.venhaserjava.model.Artista;
import org.venhaserjava.service.ArtistaService;
import java.util.List;

@Path("/v1/artistas")
@Produces("application/json")
@Consumes("application/json")
public class ArtistaResource {

    @Inject
    ArtistaService artistaService;

    @Operation(summary = "Lista todos os artistas", description = "Retorna uma lista paginada de artistas com filtros de nome e tipo")
    @APIResponse(responseCode = "200", description = "Lista retornada com sucesso")
    @GET
    public Uni<List<Artista>> listar(
        @Parameter(description = "Número da página (0-index)", example = "0")
            @QueryParam("page") @DefaultValue("0") int page, 
            @Parameter(description = "Número de registros de retorno (0-size)", example = "10")
            @QueryParam("size") @DefaultValue("10") int size,
            @Parameter(description = "Nome do Artista", example = "///")
            @QueryParam("nome") String nome,
            @Parameter(description = "Tipo do Artista", example = "Banda")
            @QueryParam("tipo") String tipo,
            @QueryParam("order") @DefaultValue("asc") String order) {
        return artistaService.listarComFiltros(page, size, nome, tipo, order);
    }

    @Operation(summary = "Cria um novo artista", description = "Requer privilégios de ADMIN. Permite associar álbuns existentes ou novos.")
    @APIResponse(responseCode = "201", description = "Artista criado com sucesso")
    @APIResponse(responseCode = "400", description = "Dados de entrada inválidos")
    @RolesAllowed("ADMIN")
    @POST
    public Uni<Response> criar(@Valid Artista artista) {
        return artistaService.salvar(artista)
                .map(novoArtista -> Response.status(Response.Status.CREATED).entity(novoArtista).build());
    }

    @Operation(summary = "Atualiza um artista existente", description = "Requer privilégios de ADMIN. Atualiza dados básicos e associações.")
    @APIResponse(responseCode = "200", description = "Artista atualizado com sucesso")
    @APIResponse(responseCode = "404", description = "Artista não encontrado")
    @RolesAllowed("ADMIN")
    @PUT
    @Path("/{id}")
    public Uni<Response> atualizar(
        @Parameter(description = "Id do Album que será atualizado.", example = "1")
        @PathParam("id") Long id,
        @Valid Artista artista
    ) {
        return artistaService.atualizar(id, artista)
                .map(atualizado -> Response.ok(atualizado).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }
    @Operation(summary = "Remove um artista", description = "Requer privilégios de ADMIN. Remove o artista e seus álbuns exclusivos (órfãos).")
    @APIResponse(responseCode = "204", description = "Artista removido com sucesso")
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN") // Requisito de segurança do edital
    public Uni<Response> deletar(
        @Parameter(description = "Id do Artista que será excluido.", example = "1")
        @PathParam("id") Long id
    ) {
        return artistaService.deletar(id)
                .map(deletado -> deletado 
                    ? Response.status(Response.Status.NO_CONTENT).build() 
                    : Response.status(Response.Status.NOT_FOUND).build());
    }

}
