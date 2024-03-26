package com.interfaces;


import org.json.JSONObject;

public interface PayLoadValidator {

    void post();
    void patch();
    void put();
    void delete();
    JSONObject get();
    void validatePayload(JSONObject payload);
    void validateDeletion();
}
