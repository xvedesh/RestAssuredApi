package com.interfaces;

import com.fasterxml.jackson.databind.util.JSONPObject;

public interface PayLoadValidator {

    void post(String s);
    void patch(String s);
    void put(String s);
    void delete(String s);
    JSONPObject get(String s);
    void validatePayload(JSONPObject payload);
    void validateDeletion(String s);
}
