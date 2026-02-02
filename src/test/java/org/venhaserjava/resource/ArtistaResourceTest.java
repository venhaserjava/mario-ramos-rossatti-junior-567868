package org.venhaserjava.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.venhaserjava.model.Artista;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;
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
            .statusCode(200)
            .body("nome", hasItem("Linkin Park")); // Nome que está no seu import-test.sql
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
    @DisplayName("Deve bloquear criação de artista para usuário comum")
    public void deveBloquearCriacaoParaUser() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"nome\":\"Intruso\"}")
        .when()
            .post("/v1/artistas")
        .then()
            .statusCode(403);
    }
}

