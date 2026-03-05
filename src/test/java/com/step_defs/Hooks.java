package com.step_defs;

import com.api.BaseTest;
import com.api.ClientAPI;
import io.cucumber.java.After;

public class Hooks {

    @After
    public void tearDown() {
        ClientAPI.clearThreadContext();
        BaseTest.clearThreadContext();
    }
}
