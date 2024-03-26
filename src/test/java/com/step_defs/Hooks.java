package com.step_defs;

import com.api.ClientAPI;
import io.cucumber.java.After;

public class Hooks {

    @After
    public void tearDown() {
        // Set Client and Client.Address to null
        ClientAPI.client = null;
        ClientAPI.address = null;
    }
}
