package com.pojo;

import java.util.LinkedHashMap;
import java.util.Map;

public class Transaction {
    private String id;
    private Object clientId;
    private String date;
    private String type;
    private int amount;
    private String currency;
    private String accountId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getClientId() {
        return clientId;
    }

    public void setClientId(Object clientId) {
        this.clientId = clientId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public Map<String, Object> generatePojoMap() {
        Map<String, Object> pojoMap = new LinkedHashMap<>();
        pojoMap.put("id", getId());
        pojoMap.put("clientId", getClientId());
        pojoMap.put("date", getDate());
        pojoMap.put("type", getType());
        pojoMap.put("amount", getAmount());
        pojoMap.put("currency", getCurrency());
        pojoMap.put("accountId", getAccountId());
        return pojoMap;
    }
}
