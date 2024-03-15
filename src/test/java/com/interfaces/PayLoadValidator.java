package com.interfaces;


import org.json.JSONObject;

public interface PayLoadValidator {

    void post(String s);
    void patch(String s);
    void put(String s);
    void delete(String s);
    JSONObject get(String s);
    void validatePayload(JSONObject payload);
    void validateDeletion(String s);
}
