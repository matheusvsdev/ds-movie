package com.devsuperior.dscommerce.controllers;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;

import com.devsuperior.dscommerce.tests.TokenUtil;
import io.restassured.http.ContentType;
import org.json.JSONException;
import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ScoreControllerRA {

    private String adminUsername, adminPassword;
    private String adminToken;
    private Long existingMovieId, nonExistingMovieId;

    private Map<String, Object> scoreRequestBody;

    @BeforeEach
    public void setup() throws JSONException {
        baseURI = "http://localhost:8080";

        adminUsername = "alex@gmail.com";
        adminPassword = "123456";

        adminToken = TokenUtil.obtainAccessToken(adminUsername, adminPassword);

        existingMovieId = 1L;
        nonExistingMovieId = 100L;

        scoreRequestBody = new HashMap<>();
        scoreRequestBody.put("movieId", 1L);
        scoreRequestBody.put("score", 4.0);
    }

    @Test
    public void saveScoreShouldReturnNotFoundWhenMovieIdDoesNotExist() throws Exception {
        scoreRequestBody.put("movieId", nonExistingMovieId);
        scoreRequestBody.put("score", 4.0);

        JSONObject newScore = new JSONObject(scoreRequestBody);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newScore)
                .when()
                .put("/scores")
                .then()
                .statusCode(404);
    }

    @Test
    public void saveScoreShouldReturnUnprocessableEntityWhenMissingMovieId() throws Exception {
        scoreRequestBody.clear();
        scoreRequestBody.put("score", 4.0);

        JSONObject newScore = new JSONObject(scoreRequestBody);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newScore)
                .when()
                .put("/scores")
                .then()
                .statusCode(422);

    }

    @Test
    public void saveScoreShouldReturnUnprocessableEntityWhenScoreIsLessThanZero() throws Exception {
        scoreRequestBody.put("movieId", existingMovieId);
        scoreRequestBody.put("score", -10.0);

        JSONObject newScore = new JSONObject(scoreRequestBody);

        given()
                .header("Content-type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(newScore)
                .when()
                .put("/scores")
                .then()
                .statusCode(422);
    }
}
