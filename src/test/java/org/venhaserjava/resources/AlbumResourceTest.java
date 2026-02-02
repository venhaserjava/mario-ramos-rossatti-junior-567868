package org.venhaserjava.resources;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.venhaserjava.model.Album;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class AlbumResourceTest {

    @Test
    @DisplayName("Deve retornar 401 ao tentar criar álbum sem estar autenticado")
    public void deveRetornar401AoCriarSemAutenticacao() {
        Album novoAlbum = new Album();
        novoAlbum.titulo = "Álbum Proibido";
        novoAlbum.anoLancamento = 2024;

        given()
            .contentType(ContentType.JSON)
            .body(novoAlbum)
        .when()
            .post("/v1/albuns/artista/1")
        .then()
            .statusCode(401); 
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"ADMIN"})
    @DisplayName("Deve permitir criar álbum quando o usuário for ADMIN")
    public void devePermitirCriarComRoleAdmin() {
        Album novoAlbum = new Album();
        novoAlbum.titulo = "Álbum de Teste ADMIN";
        novoAlbum.anoLancamento = 2026;

        given()
            .contentType(ContentType.JSON)
            .body(novoAlbum)
        .when()
            .post("/v1/albuns/artista/1") // Certifique-se que o artista ID 1 existe no seu import.sql ou DB de teste
        .then()
            .statusCode(201)
            .body("titulo", is("Álbum de Teste ADMIN"));
    }

    @Test
    @TestSecurity(user = "comum-user", roles = {"USER"})
    @DisplayName("Deve retornar 403 ao tentar criar álbum com role insuficiente")
    public void deveRetornar403ComRoleIncorreta() {
        Album novoAlbum = new Album();
        novoAlbum.titulo = "Tentativa Hacker";

        given()
            .contentType(ContentType.JSON)
            .body(novoAlbum)
        .when()
            .post("/v1/albuns/artista/1")
        .then()
            .statusCode(403);
    }

    @Test
    @TestSecurity(user = "admin-user", roles = {"ADMIN"})
    @DisplayName("Deve validar erro 400 ao tentar criar álbum para artista inexistente")
    public void deveRetornar400ParaArtistaInexistente() {
        Album novoAlbum = new Album();
        novoAlbum.titulo = "Álbum Sem Dono";
        novoAlbum.anoLancamento = 2024;

        given()
            .contentType(ContentType.JSON)
            .body(novoAlbum)
        .when()
            .post("/v1/albuns/artista/999") // ID que não existe no import-test.sql
        .then()
            .statusCode(400);
    }
    
    @Test
    @TestSecurity(user = "admin-user", roles = {"ADMIN"})
    @DisplayName("Deve deletar um álbum com sucesso sendo ADMIN")
    public void deveDeletarAlbumSendoAdmin() {
            // 1. Primeiro criamos um álbum para ter o que deletar (garantindo o ID)
            // Nota: Em testes de integração reais, poderíamos inserir via SQL no import-test.sql
            
            given()
                .pathParam("id", 1) // Supondo que o ID 1 seja criado ou já exista
            .when()
                .delete("/v1/albuns/{id}")
            .then()
                .statusCode(204); // No Content (sucesso na deleção)
    }



}
