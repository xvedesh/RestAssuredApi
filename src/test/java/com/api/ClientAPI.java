package com.api;

import com.pojo.Client;
import io.restassured.response.Response;
import org.json.*;
import com.github.javafaker.Faker;
import com.interfaces.PayLoadValidator;
import org.junit.Assert;

import java.util.*;

import static io.restassured.RestAssured.given;

public class ClientAPI extends BaseTest implements PayLoadValidator {
    static Faker faker = new Faker();
    public static Client client = new Client();
    public static Client.Address address = new Client.Address();
    static boolean deletionSuccessful = false;

    @Override
    public void post() {
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
    public void patch() {
        given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .pathParam("clientId", client.getId())
                .body(patchBody())
                .when()
                .patch(baseURI + allClientsEndPoint + clientEndPoint)
                .prettyPeek()
                .path("accessToken");
    }

    @Override
    public void put() {
        given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .pathParam("clientId", client.getId())
                .body(putBody())
                .when()
                .put(baseURI + allClientsEndPoint + clientEndPoint)
                .prettyPeek()
                .path("accessToken");
    }

    @Override
    public void delete() {
        Response response = given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .pathParam("clientId", client.getId())
                .when()
                .delete(baseURI + allClientsEndPoint + clientEndPoint)
                .prettyPeek();
        if(response.getStatusCode() == 200) {
            deletionSuccessful = true;
        }
    }

    @Override
    public JSONObject get() {
        Response response = given()
                .log()
                .all()
                .headers(returnAuthHeaders())
                .pathParam("clientId", client.getId())
                .when()
                .get(baseURI + allClientsEndPoint + clientEndPoint)
                .prettyPeek();

        String responseBody = response.getBody().asString();
        return new JSONObject(responseBody);
    }

    @Override
    public void validatePayload(JSONObject payload) {

        // Define a list of attributes
        List<String> attributes = getAttributeList();

        // Generate maps from the POJO and the response payload
        Map<String, Object> pojoMap = client.generatePojoMap();
        Map<String, Object> responseMap = generateResponseMap(payload);

        // Iterate through the attributes and validate values
        for (String attribute : attributes) {
            Object expectedValue = pojoMap.get(attribute);
            Object actualValue = responseMap.get(attribute);
            if (attribute.equals("id") && expectedValue instanceof UUID) {
                Assert.assertEquals("Value mismatch for attribute: " + attribute,
                        ((UUID) expectedValue).toString(), actualValue);
            } else {
                Assert.assertEquals("Value mismatch for attribute: " + attribute, expectedValue, actualValue);
            }
        }
    }

    @Override
    public void validateDeletion() {
        Assert.assertTrue(deletionSuccessful);
    }

    private static Client returnBody() {

        address.setStreet(faker.address().streetAddress());
        address.setCity(faker.address().cityName());
        address.setState(faker.address().stateAbbr());
        address.setZipCode(faker.numerify("#####"));

        client.setId(UUID.randomUUID());
        client.setFirstName(faker.name().firstName());
        client.setLastName(faker.name().lastName());
        client.setEmail(faker.internet().emailAddress());
        client.setPhone(faker.numerify("###-###-####"));
        client.setAddress(address);

        return client;
    }

    private static Client putBody() {

        address.setStreet(faker.address().streetAddress());
        address.setCity(faker.address().cityName());
        address.setState(faker.address().stateAbbr());
        address.setZipCode(faker.numerify("#####"));

        client.setFirstName(faker.name().firstName());
        client.setLastName(faker.name().lastName());
        client.setEmail(faker.internet().emailAddress());
        client.setPhone(faker.numerify("###-###-####"));
        client.setAddress(address);

        return client;
    }

    private static Map<String, Object> patchBody() {

        Map<String, Object> map = new LinkedHashMap<>();
        Map<String, Object> addressUpdate = new LinkedHashMap<>();

        addressUpdate.put("street", faker.address().streetAddress());
        addressUpdate.put("city", faker.address().cityName());
        addressUpdate.put("state", faker.address().stateAbbr());
        addressUpdate.put("zipCode", faker.numerify("#####"));

        map.put("address", addressUpdate);

        address.setStreet((String) addressUpdate.get("street"));
        address.setCity((String) addressUpdate.get("city"));
        address.setState((String) addressUpdate.get("state"));
        address.setZipCode((String) addressUpdate.get("zipCode"));

        return map;
    }

    private Map<String, Object> generateResponseMap(JSONObject payload) {
        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("id", payload.getString("id"));
        responseMap.put("firstName", payload.getString("firstName"));
        responseMap.put("lastName", payload.getString("lastName"));
        responseMap.put("email", payload.getString("email"));
        responseMap.put("phone", payload.getString("phone"));

        // Add address attributes to the map
        JSONObject addressObject = payload.getJSONObject("address");
        responseMap.put("street", addressObject.getString("street"));
        responseMap.put("city", addressObject.getString("city"));
        responseMap.put("state", addressObject.getString("state"));
        responseMap.put("zipCode", addressObject.getString("zipCode"));

        return responseMap;
    }

    public List<String> getAttributeList() {
        return Arrays.asList(
                "id",
                "firstName",
                "lastName",
                "email",
                "phone",
                "street",
                "city",
                "state",
                "zipCode");
    }
}
