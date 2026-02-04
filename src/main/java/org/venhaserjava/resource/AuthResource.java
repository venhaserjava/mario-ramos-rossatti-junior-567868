package org.venhaserjava.resource;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.annotation.security.PermitAll;
import org.venhaserjava.dto.LoginRequest;
import org.venhaserjava.dto.TokenResponse;    
import io.smallrye.jwt.build.Jwt;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;



@Path("/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    @PermitAll
    public Response login(LoginRequest login) {
        // Simulação de autenticação (em produção, buscaria no banco)
        if ("admin".equals(login.username()) && "admin123".equals(login.password())) {
            String token = Jwt.issuer("https://venhaserjava.org/issuer")
                    .upn(login.username())
                    .groups(Set.of("ADMIN"))
                    .expiresIn(Duration.ofMinutes(5)) // RIGOROSAMENTE 5 MINUTOS
                    .sign();

            // Geramos um Refresh Token simples (pode ser outro JWT mais longo ou um UUID)
            String refreshToken = UUID.randomUUID().toString(); 

            return Response.ok(new TokenResponse(token, refreshToken)).build();
        }
        return Response.status(Response.Status.UNAUTHORIZED).build();
    }

    @POST
    @Path("/refresh")
    @PermitAll
    public Response refresh(@HeaderParam("Authorization") String oldToken, String refreshToken) {
        // Lógica de renovação: Aqui você validaria se o refreshToken é válido no seu banco/cache
        // Se sim, gera um novo Access Token de 5 minutos.
        String newToken = Jwt.issuer("https://venhaserjava.org/issuer")
                .upn("admin")
                .groups(Set.of("ADMIN"))
                .expiresIn(Duration.ofMinutes(5))
                .sign();
        
        return Response.ok(new TokenResponse(newToken, refreshToken)).build();
    }
}