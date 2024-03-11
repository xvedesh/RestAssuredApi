package com.api;

import com.utils.ConfigurationReader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BaseTest {

    private static String token = "";
    public static final String baseURI = ConfigurationReader.getProperty("baseURI");
    private static final String authEndPoint = ConfigurationReader.getProperty("authEndPoint");
    public static final String clientEndPoint = ConfigurationReader.getProperty("clientEndPoint");
    public static final String accountsEndPoint = ConfigurationReader.getProperty("accountEndPoint");
    public static final String transactionsEndPoint = ConfigurationReader.getProperty("transactionEndPoint");
    public static final String portfolioEndPoint = ConfigurationReader.getProperty("portfolioEndPoint");

    public static void generateToken() {
        token = "Bearer " + given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .body(returnCredentials())
                .when()
                .post(baseURI + authEndPoint)
                .prettyPeek()
                .path("accessToken");
    }


    public static Map<String, String> returnCredentials() {
        Map<String,String> map = new LinkedHashMap<>();
        map.put("username", ConfigurationReader.getProperty("username"));
        map.put("password", ConfigurationReader.getProperty("password"));

        return map;
    }

    public static Map<String, String> returnAuthHeaders() {
        Map<String,String> map = new HashMap<>();
        map.put("Authorization", token);
        map.put("Content-Type", "application/json");
        return map;
    }
}
