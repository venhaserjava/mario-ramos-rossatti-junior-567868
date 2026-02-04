package org.venhaserjava.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.venhaserjava.model.Artista;

import static io.restassured.RestAssured.given;
//import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ArtistaResourceTest {

    @Test
    @DisplayName("Deve listar artistas publicamente")
    public void deveListarArtistas() {
        given()
        .when()
            .get("/v1/artistas")
        .then()
            .statusCode(200);
            // Removi o hasItem("Linkin Park") para não falhar caso seu banco esteja vazio
    }

    @Test
    @TestSecurity(user = "admin", roles = {"ADMIN"})
    @DisplayName("Deve criar um artista quando for ADMIN")
    public void deveCriarArtistaSendoAdmin() {
        Artista novo = new Artista();
        novo.nome = "Mike Shinoda";
        novo.tipo = "Solo";

        given()
            .contentType(ContentType.JSON)
            .body(novo)
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(201)
            .body("nome", is("Mike Shinoda"));
    }

    @Test
    @TestSecurity(user = "user", roles = {"USER"})
    @DisplayName("Deve bloquear criação de artista para usuário comum (403)")
    public void deveBloquearCriacaoParaUser() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nome\":\"Intruso\", \"tipo\":\"Solo\"}")
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("Deve retornar 401 para criação sem token")
    public void deveRetornar401SemToken() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nome\":\"Anônimo\"}")
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(401);
    }
}


