package com.luanjining.rag.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "rag.dify")
public class RagConfiguration {

    // Getters and Setters
    private String authorizationDataset;
    private String authorizationApp;
    private String baseUrl;
    private String datasetId;
    private String userId;

}