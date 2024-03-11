package com.api;

import com.interfaces.PayLoadValidator;
import com.utils.ConfigurationReader;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

public class APIMap {

    private static final Map<String, Supplier<PayLoadValidator>> apiValidatorSuppliers = new HashMap<>();
    private static final Map<String, PayLoadValidator> apiValidatorsMap = new ConcurrentHashMap<>();

    static {
        apiValidatorSuppliers.put("client", ClientAPI::new);
        apiValidatorSuppliers.put("account", AccountAPI::new);
        apiValidatorSuppliers.put("portfolio", PortfolioAPI::new);
        apiValidatorSuppliers.put("transaction", TransactionAPI::new);
    }

    public static PayLoadValidator getApiValidator(String block) {
        return apiValidatorsMap.computeIfAbsent(block.toLowerCase(), blockKey -> {
            Supplier<PayLoadValidator> supplier = apiValidatorSuppliers.get(blockKey);
            if(supplier == null)
                throw new IllegalArgumentException("No apiValidator found for : " + blockKey);
            return supplier.get();
        });
    }

}
