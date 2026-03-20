package com.pojo;

import java.util.LinkedHashMap;
import java.util.Map;

public class PortfolioAsset {
    private String assetId;
    private String type;
    private String name;
    private int quantity;
    private int currentValue;

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public Map<String, Object> generatePojoMap() {
        Map<String, Object> assetMap = new LinkedHashMap<>();
        assetMap.put("assetId", getAssetId());
        assetMap.put("type", getType());
        assetMap.put("name", getName());
        assetMap.put("quantity", getQuantity());
        assetMap.put("currentValue", getCurrentValue());
        return assetMap;
    }
}
