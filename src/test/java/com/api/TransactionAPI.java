package com.api;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.interfaces.PayLoadValidator;
import org.json.JSONObject;

public class TransactionAPI implements PayLoadValidator {
    @Override
    public void post(String s) {

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
    public JSONObject get(String s) {
        return null;
    }

    @Override
    public void validatePayload(JSONObject payload) {

    }

    @Override
    public void validateDeletion(String s) {

    }
}
