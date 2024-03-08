package com.api;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.interfaces.PayLoadValidator;

public class ClientAPI implements PayLoadValidator {
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
    public JSONPObject get(String s) {
        return null;
    }

    @Override
    public void validatePayload(JSONPObject payload) {

    }

    @Override
    public void validateDeletion(String s) {

    }
}
