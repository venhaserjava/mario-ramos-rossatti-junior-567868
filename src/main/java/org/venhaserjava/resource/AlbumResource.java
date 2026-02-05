


package org.venhaserjava.resource;

import java.util.List;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;


import org.venhaserjava.dto.AlbumResponseDTO;
import org.venhaserjava.model.Album;
import org.venhaserjava.service.AlbumService;


// 
// Endpoint para operações relacionadas aos álbuns musicais.
// Utiliza DTOs na saída para garantir a integridade do contrato e evitar recursão infinita no JSON.
// 
@Path("/v1/albuns")
@Produces("application/json")
@Consumes("application/json")
public class AlbumResource {

    @Inject
    AlbumService albumService;

    @Operation(summary = "Busca álbuns por título", description = "Retorna uma lista de álbuns e seus respectivos artistas (DTO)")
    @APIResponse(responseCode = "200", description = "Busca realizada com sucesso")
    @GET
    public Uni<List<AlbumResponseDTO>> buscar(
            @Parameter(description = "Titulo ou nome do Album", example = "///")
            @QueryParam("titulo") @DefaultValue("") String titulo,
            @Parameter(description = "Número da página (0-index)", example = "0")
            @QueryParam("page") @DefaultValue("0") int page,
            @Parameter(description = "Número de registros de retorno (0-size)", example = "10")
            @QueryParam("size") @DefaultValue("10") int size
        ) {
        return albumService.buscarPorTitulo(titulo, page, size);
    }

    @Operation(summary = "Cria um álbum para um artista", description = "Requer ADMIN. Vincula o novo álbum ao ID do artista fornecido.")
    @APIResponse(responseCode = "201", description = "Álbum criado e vinculado com sucesso")
    @POST
    @Path("/artista/{artistaId}")
    @RolesAllowed("ADMIN")
    public Uni<Response> criar(
        @Parameter(description = "Id do Artista que o album será associado.", example = "1")
        @PathParam("artistaId") Long artistaId,         
        @Valid Album album
    ) {
        return albumService.criarComArtista(artistaId, album)
                .map(novoAlbum -> Response.status(Response.Status.CREATED).entity(novoAlbum).build());
    }

    @Operation(summary = "Atualiza um artista existente", description = "Requer privilégios de ADMIN. Atualiza dados básicos e associações.")
    @APIResponse(responseCode = "200", description = "Artista atualizado com sucesso")
    @APIResponse(responseCode = "404", description = "Artista não encontrado")
    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> atualizar(
        @Parameter(description = "Id do Album que será atualizado.", example = "1")
        @PathParam("id") Long id,
        @Valid Album album
    ) {
        return albumService.atualizar(id, album)
                .map(atualizado -> Response.ok(atualizado).build())
                .onItem().ifNull().continueWith(Response.status(Response.Status.NOT_FOUND).build());
    }


    @Operation(summary = "Remove um Album", description = "Requer privilégios de ADMIN. Remove o album.")
    @APIResponse(responseCode = "204", description = "Album removido com sucesso")
    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Uni<Response> deletar(
        @Parameter(description = "Id do Album que será excluido.", example = "1")
        @PathParam("id") Long id
    )  {
        // id é do álbum a ser deletado
        return albumService.deletarAlbum(id)
                .replaceWith(Response.noContent().build());
    }
}