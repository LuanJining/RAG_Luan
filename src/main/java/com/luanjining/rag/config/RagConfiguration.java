package com.luanjining.rag.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rag.dify")
public class RagConfiguration {

    private String authorizationDataset;
    private String authorizationApp;
    private String userId;
    private String baseUrl;
    private String datasetId;

    // Getters and Setters
    public String getAuthorizationDataset() {
        return authorizationDataset;
    }

    public void setAuthorizationDataset(String authorizationDataset) {
        this.authorizationDataset = authorizationDataset;
    }

    public String getAuthorizationApp() {
        return authorizationApp;
    }

    public void setAuthorizationApp(String authorizationApp) {
        this.authorizationApp = authorizationApp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }
}