package com.api;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.javafaker.Faker;
import com.interfaces.PayLoadValidator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
                .post(baseURI + clientEndPoint)
                .prettyPeek()
                .path("accessToken");
    }

    @Override
    public void patch(String s) {

    }

    @Override
    public void put(String s) {

    }

    @Override
    public void delete(String s) {

    }

    @Override
    public JSONPObject get(String s) {
        return null;
    }

    @Override
    public void validatePayload(JSONPObject payload) {

    }

    @Override
    public void validateDeletion(String s) {

    }

    private static Map<String, Object> returnBody() {
        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Object> address = new LinkedHashMap<>();
        map.put("id", faker.number().randomDigitNotZero());
        map.put("firstName", faker.name().firstName());
        map.put("lastName", faker.name().lastName());
        map.put("email", faker.internet().emailAddress());
        map.put("phone", faker.numerify("###-###-####"));
        address.put("street", faker.address().streetAddress());
        address.put("city", faker.address().cityName());
        address.put("state", faker.address().stateAbbr());
        address.put("zipCode", faker.numerify("#####"));
        map.put("address", address);
        return map;
        }
}
