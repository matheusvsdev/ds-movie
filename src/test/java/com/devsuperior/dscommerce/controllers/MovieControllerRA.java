package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class MovieControllerRA {

    private String clientUsername, clientPassword, adminUsername, adminPassword;
    private String adminToken, clientToken, invalidToken;
    private Long existingMovieId, nonExistingMovieId;
    private String movieName;

    private Map<String, Object> postMovieInstance;

    @BeforeEach
    public void setup() throws JSONException {
        baseURI = "http://localhost:8080";

        clientUsername = "joao@gmail.com";
        clientPassword = "123456";
        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        clientToken = TokenUtil.obtainAccessToken(clientUsername, clientPassword);
        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);
        invalidToken = adminToken + "xpto";

        movieName = "The Witcher";

        postMovieInstance = new HashMap<>();
        postMovieInstance.put("title", " ");
        postMovieInstance.put("image", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postMovieInstance.put("score", 0.0);
        postMovieInstance.put("count", 0);
    }

    @Test
    public void findAllShouldReturnOkWhenMovieNoArgumentsGiven() {
        given()
                .get("/movies")
                .then()
                .statusCode(200);
    }

    @Test
    public void findAllShouldReturnPageMoviesWhenMovieTitleParamIsNotEmpty() {
        given()
                .get("/movies?title={movieName}", movieName)
                .then()
                .statusCode(200)
                .body("content.id[0]", is(1))
                .body("content.title[0]", equalTo("The Witcher"))
                .body("content.score[0]", is(4F))
                .body("content.count[0]", is(3))
                .body("content.image[0]", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/jBJWaqoSCiARWtfV0GlqHrcdidd.jpg"));
    }

    @Test
    public void findAllShouldReturnMovieWhenIdExists() {
        existingMovieId = 2L;

        given()
                .get("/movies/{id}", existingMovieId)
                .then()
                .statusCode(200)
                .body("id", is(2))
                .body("title", equalTo("Venom: Tempo de Carnificina"))
                .body("image", equalTo("https://www.themoviedb.org/t/p/w533_and_h300_bestv2/vIgyYkXkg6NC2whRbYjBD7eb3Er.jpg"))
                .body("score", is(3.3F))
                .body("count", is(3));
    }

    @Test
    public void findByIdShouldReturnNotFoundWhenIdDoesNotExist() {

        nonExistingMovieId = 100L;

        given()
                .get("/movies/{id}", nonExistingMovieId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Recurso não encontrado"))
                .body("status", equalTo(404));
    }

    @Test
    public void insertShouldReturnUnprocessableEntityWhenAdminLoggedAndBlankTitle() throws JSONException {
        postMovieInstance.put("title", "   ");
        JSONObject newMovie = new JSONObject(postMovieInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .body(newMovie)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/movies")
                .then()
                .statusCode(422);
    }

    @Test
    public void insertShouldReturnForbiddenWhenClientLogged() throws Exception {
        postMovieInstance.put("title", "Valid Title");
        postMovieInstance.put("image", "https://raw.githubusercontent.com/devsuperior/dscatalog-resources/master/backend/img/1-big.jpg");
        postMovieInstance.put("score", 4.5);
        postMovieInstance.put("count", 10);

        JSONObject newMovie = new JSONObject(postMovieInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + clientToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newMovie)
                .when()
                .post("/movies")
                .then()
                .statusCode(403);
    }

    @Test
    public void insertShouldReturnUnauthorizedWhenInvalidToken() throws Exception {
        JSONObject newMovie = new JSONObject(postMovieInstance);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newMovie)
                .when()
                .post("/movies")
                .then()
                .statusCode(401);
    }
}
