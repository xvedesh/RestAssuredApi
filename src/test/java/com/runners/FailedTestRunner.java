package com.runners;

import com.analysis.failure.FailureAnalysisExecutionListener;
import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;

@Listeners(FailureAnalysisExecutionListener.class)
@CucumberOptions(
        features = "@target/rerun.txt",
        glue = "com.step_defs"
)
public class FailedTestRunner extends AbstractTestNGCucumberTests {
    @Override
    @DataProvider(parallel = false)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
