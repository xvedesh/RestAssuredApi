package com.interfaces;


import io.restassured.response.Response;
import org.json.JSONObject;

public interface PayLoadValidator {

    void post();
    void patch();
    void put();
    void delete();
    JSONObject get();
    void validatePayload(JSONObject payload);
    void validateDeletion();

    default String getCurrentEntityId() {
        throw new UnsupportedOperationException("Current entity id is not implemented");
    }

    default String getEntityType() {
        throw new UnsupportedOperationException("Entity type is not implemented");
    }

    default Response fetchLatestResponse() {
        throw new UnsupportedOperationException("Latest response is not implemented");
    }

    default JSONObject fetchLatestResponseBodyAsJsonObject() {
        return new JSONObject(fetchLatestResponse().getBody().asString());
    }
}
