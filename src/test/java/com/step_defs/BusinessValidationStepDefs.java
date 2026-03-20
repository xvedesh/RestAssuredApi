package com.step_defs;

import com.api.AccountAPI;
import com.api.APIMap;
import com.api.ClientAPI;
import com.api.PortfolioAPI;
import com.api.TransactionAPI;
import com.interfaces.PayLoadValidator;
import com.utils.TestLogger;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.json.JSONObject;
import org.testng.Assert;

import java.util.UUID;

public class BusinessValidationStepDefs {
    private final ClientAPI clientAPI = new ClientAPI();
    private final AccountAPI accountAPI = new AccountAPI();
    private final PortfolioAPI portfolioAPI = new PortfolioAPI();
    private final TransactionAPI transactionAPI = new TransactionAPI();

    @Given("I prepare the current account for the current client")
    public void i_prepare_the_current_account_for_the_current_client() {
        AccountAPI.useClientId(ClientAPI.getCurrentClientId());
    }

    @Given("I prepare the current portfolio for the current client")
    public void i_prepare_the_current_portfolio_for_the_current_client() {
        PortfolioAPI.useClientId(ClientAPI.getCurrentClientId());
    }

    @Given("I prepare the current transaction for the current client and account")
    public void i_prepare_the_current_transaction_for_the_current_client_and_account() {
        TransactionAPI.useClientId(ClientAPI.getCurrentClientId());
        TransactionAPI.useAccountId(AccountAPI.getCurrentAccountId());
    }

    @Given("I create a new client with a dependent account")
    public void i_create_a_new_client_with_a_dependent_account() {
        clientAPI.post();
        AccountAPI.useClientId(ClientAPI.getCurrentClientId());
        accountAPI.post();
    }

    @Given("I create a new client with a dependent portfolio")
    public void i_create_a_new_client_with_a_dependent_portfolio() {
        clientAPI.post();
        PortfolioAPI.useClientId(ClientAPI.getCurrentClientId());
        portfolioAPI.post();
    }

    @Given("I create a new client with a dependent transaction")
    public void i_create_a_new_client_with_a_dependent_transaction() {
        clientAPI.post();
        AccountAPI.useClientId(ClientAPI.getCurrentClientId());
        accountAPI.post();
        TransactionAPI.useClientId(ClientAPI.getCurrentClientId());
        TransactionAPI.useAccountId(AccountAPI.getCurrentAccountId());
        transactionAPI.post();
    }

    @Given("I create a new account with a dependent transaction for the current client")
    public void i_create_a_new_account_with_a_dependent_transaction_for_the_current_client() {
        AccountAPI.useClientId(ClientAPI.getCurrentClientId());
        accountAPI.post();
        TransactionAPI.useClientId(ClientAPI.getCurrentClientId());
        TransactionAPI.useAccountId(AccountAPI.getCurrentAccountId());
        transactionAPI.post();
    }

    @When("I create an account with non-existing clientId")
    public void i_create_an_account_with_non_existing_client_id() {
        AccountAPI.useClientId(randomMissingClientId());
        accountAPI.post();
    }

    @When("I update the current account with non-existing clientId")
    public void i_update_the_current_account_with_non_existing_client_id() {
        AccountAPI.useClientId(randomMissingClientId());
        accountAPI.put();
    }

    @When("I create a portfolio with non-existing clientId")
    public void i_create_a_portfolio_with_non_existing_client_id() {
        PortfolioAPI.useClientId(randomMissingClientId());
        portfolioAPI.post();
    }

    @When("I patch the current portfolio with non-existing clientId")
    public void i_patch_the_current_portfolio_with_non_existing_client_id() {
        PortfolioAPI.useClientId(randomMissingClientId());
        portfolioAPI.patch();
    }

    @When("I create a transaction with non-existing clientId")
    public void i_create_a_transaction_with_non_existing_client_id() {
        TransactionAPI.useClientId(randomMissingClientId());
        TransactionAPI.useAccountId("A123456");
        transactionAPI.post();
    }

    @When("I create a transaction with non-existing accountId")
    public void i_create_a_transaction_with_non_existing_account_id() {
        TransactionAPI.useClientId(1);
        TransactionAPI.useAccountId(randomMissingAccountId());
        transactionAPI.post();
    }

    @When("I create a transaction with mismatched clientId and account owner")
    public void i_create_a_transaction_with_mismatched_client_id_and_account_owner() {
        TransactionAPI.useClientId(2);
        TransactionAPI.useAccountId("A123456");
        transactionAPI.post();
    }

    @When("I update the current transaction with invalid account and client relationship")
    public void i_update_the_current_transaction_with_invalid_account_and_client_relationship() {
        TransactionAPI.useClientId(2);
        TransactionAPI.useAccountId("A123456");
        transactionAPI.put();
    }

    @Then("the API error response for {string} should be {int} and contain {string}")
    public void the_api_error_response_for_should_be_and_contain(String block, Integer expectedStatusCode, String expectedMessage) {
        PayLoadValidator validator = APIMap.getApiValidator(block);
        Assert.assertNotNull(validator.fetchLatestResponse(), "No API response is available for validation");

        String responseBody = validator.fetchLatestResponse().getBody().asPrettyString();
        TestLogger.attach(String.format(
                "entity=%s%nexpectedStatusCode=%d%nactualStatusCode=%d%nresponseBody=%s",
                block,
                expectedStatusCode,
                validator.fetchLatestResponse().statusCode(),
                responseBody
        ), "HTTP Error Assertions");

        Assert.assertEquals(validator.fetchLatestResponse().statusCode(), expectedStatusCode.intValue(),
                "Unexpected HTTP status code");
        Assert.assertTrue(responseBody.contains(expectedMessage),
                "Expected error message not found in response body");
    }

    @Then("the current account should reference the current client")
    public void the_current_account_should_reference_the_current_client() {
        JSONObject accountPayload = accountAPI.get();
        Assert.assertEquals(String.valueOf(accountPayload.get("clientId")), ClientAPI.getCurrentClientId(),
                "Account should reference the current client");
    }

    @Then("the current transaction should reference the current client and account")
    public void the_current_transaction_should_reference_the_current_client_and_account() {
        JSONObject transactionPayload = transactionAPI.get();
        Assert.assertEquals(String.valueOf(transactionPayload.get("clientId")), ClientAPI.getCurrentClientId(),
                "Transaction should reference the current client");
        Assert.assertEquals(transactionPayload.getString("accountId"), AccountAPI.getCurrentAccountId(),
                "Transaction should reference the current account");
    }

    private String randomMissingClientId() {
        return "missing-client-" + UUID.randomUUID();
    }

    private String randomMissingAccountId() {
        return "A" + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
