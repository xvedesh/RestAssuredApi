package com.step_defs;

import com.api.APIMap;
import com.api.BaseTest;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class ClientDataStepDefs {
    @Given("I send POST request to get Authorization Token")
    public void i_send_post_request_to_get_authorization_token() {
        BaseTest.generateToken();
    }

    @Then("I send POST request to create new {string} data")
    public void i_send_post_request_to_create_new_data(String block) {
        APIMap.getApiValidator(block).post();
    }

    @Then("I send GET request to {string} data service, and validate created data")
    public void i_send_get_request_to_data_service_and_validate_created_data(String block) {
        APIMap.getApiValidator(block).validatePayload(APIMap.getApiValidator(block).get());
    }

    @Then("I send PUT request to update  crated {string} data and validate updated data")
    public void i_send_put_request_to_update_crated_data_and_validate_updated_data(String block) {
        APIMap.getApiValidator(block).put();
        APIMap.getApiValidator(block).validatePayload(APIMap.getApiValidator(block).get());
    }

    @Then("I send PATCH request to update created {string} data and validate updated data")
    public void i_send_patch_request_to_update_created_data_and_validate_updated_data(String block) {
        APIMap.getApiValidator(block).patch();
        APIMap.getApiValidator(block).validatePayload(APIMap.getApiValidator(block).get());
    }

    @Then("I send DELETE request to delete created {string} data validate data removal")
    public void i_send_delete_request_to_delete_created_data_validate_data_removal(String block) {
        APIMap.getApiValidator(block).delete();
        APIMap.getApiValidator(block).validateDeletion();
    }
}
