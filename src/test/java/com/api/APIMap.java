package com.api;

import com.interfaces.PayLoadValidator;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class APIMap {

    private static final Map<String, Supplier<PayLoadValidator>> apiValidatorSuppliers = new HashMap<>();

    static {
        apiValidatorSuppliers.put("client", ClientAPI::new);
        apiValidatorSuppliers.put("account", AccountAPI::new);
        apiValidatorSuppliers.put("portfolio", PortfolioAPI::new);
        apiValidatorSuppliers.put("transaction", TransactionAPI::new);
    }

    public static PayLoadValidator getApiValidator(String block) {
        Supplier<PayLoadValidator> supplier = apiValidatorSuppliers.get(block.toLowerCase());
        if(supplier == null)
            throw new IllegalArgumentException("No apiValidator found for : " + block.toLowerCase());
        return supplier.get();
    }

}
