package com.api;

import com.fasterxml.jackson.databind.util.JSONPObject;
import org.json.*;
import com.github.javafaker.Faker;
import com.interfaces.PayLoadValidator;

import java.util.LinkedHashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ClientAPI extends BaseTest implements PayLoadValidator {
    static Faker faker = new Faker();

    @Override
    public void post(String s) {
        given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .body(returnBody())
                .when()
                .post(baseURI + allClientsEndPoint)
                .prettyPeek()
                .path("accessToken");
    }

    @Override
    public void patch(String s) {

    }

    @Override
    public void put(String s) {
        given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .pathParam("clientId", "1")
                //ToDo: Make client id dynamic and corresponding to client Id that was in POST request before it
                .body(returnBody())
                .when()
                .put(baseURI + allClientsEndPoint + clientEndPoint)
                .prettyPeek()
                .path("accessToken");
    }

    @Override
    public void delete(String s) {

    }

    @Override
    public JSONObject get(String s) {
        return null;
    }

    @Override
    public void validatePayload(JSONObject payload) {

    }

    @Override
    public void validateDeletion(String s) {

    }

    private static Map<String, Object> returnBody() {

        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Object> address = new LinkedHashMap<>();

        address.put("street", faker.address().streetAddress());
        address.put("city", faker.address().cityName());
        address.put("state", faker.address().stateAbbr());
        address.put("zipCode", faker.numerify("#####"));

        map.put("id", faker.number().randomDigitNotZero());
        map.put("firstName", faker.name().firstName());
        map.put("lastName", faker.name().lastName());
        map.put("email", faker.internet().emailAddress());
        map.put("phone", faker.numerify("###-###-####"));
        map.put("address", address);

        return map;
    }
}
