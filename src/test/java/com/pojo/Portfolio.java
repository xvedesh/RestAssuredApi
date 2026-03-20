package com.pojo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Portfolio {
    private String id;
    private Object clientId;
    private String name;
    private List<PortfolioAsset> assets = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PortfolioAsset> getAssets() {
        return assets;
    }

    public void setAssets(List<PortfolioAsset> assets) {
        this.assets = assets;
    }

    public Map<String, Object> generatePojoMap() {
        Map<String, Object> pojoMap = new LinkedHashMap<>();
        pojoMap.put("id", getId());
        pojoMap.put("clientId", getClientId());
        pojoMap.put("name", getName());
        pojoMap.put("assets", getAssets().stream()
                .map(PortfolioAsset::generatePojoMap)
                .collect(Collectors.toList()));
        return pojoMap;
    }
}
