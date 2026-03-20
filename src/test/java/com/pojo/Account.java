package com.pojo;

import java.util.LinkedHashMap;
import java.util.Map;

public class Account {
    private String id;
    private Object clientId;
    private String type;
    private int balance;
    private String currency;
    private String creationDate;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, Object> generatePojoMap() {
        Map<String, Object> pojoMap = new LinkedHashMap<>();
        pojoMap.put("id", getId());
        pojoMap.put("clientId", getClientId());
        pojoMap.put("type", getType());
        pojoMap.put("balance", getBalance());
        pojoMap.put("currency", getCurrency());
        pojoMap.put("creationDate", getCreationDate());
        return pojoMap;
    }
}
