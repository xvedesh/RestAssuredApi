package com.step_defs;

import com.api.BaseTest;
import com.api.AccountAPI;
import com.api.ClientAPI;
import com.api.PortfolioAPI;
import com.api.TransactionAPI;
import com.kafka.ScenarioContext;
import com.utils.TestLogger;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.After;

public class Hooks {

    @Before
    public void setUpScenarioLogging(Scenario scenario) {
        TestLogger.setScenarioName(scenario.getName());
        TestLogger.setCucumberScenario(scenario);
        TestLogger.log("Starting scenario");
    }

    @After
    public void tearDown() {
        TestLogger.log("Cleaning up scenario context");
        ScenarioContext.clear();
        AccountAPI.clearThreadContext();
        ClientAPI.clearThreadContext();
        PortfolioAPI.clearThreadContext();
        TransactionAPI.clearThreadContext();
        BaseTest.clearThreadContext();
        TestLogger.clear();
    }
}
