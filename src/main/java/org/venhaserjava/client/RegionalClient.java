package org.venhaserjava.client;


import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.venhaserjava.dto.RegionalDTO;
import java.util.List;

@RegisterRestClient(baseUri = "https://integrador-argus-api.geia.vip/v1")
public interface RegionalClient {

    @GET
    @Path("/regionais")
    Uni<List<RegionalDTO>> buscarRegionaisExternas();
}