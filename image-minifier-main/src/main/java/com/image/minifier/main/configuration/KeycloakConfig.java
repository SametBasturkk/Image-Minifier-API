package com.image.minifier.main.configuration;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class KeycloakConfig {


    public static final String REALM = "minifier";
    public static final String SERVER_URL = "http://localhost:8081/auth";
    public static final String CLIENT_ID = "admin-cli";
    public static final String CLIENT_SECRET = "TRXOHE4LbDRHtgoPMxVFBqBvT2dwhCZy";
    public static final String GRANT_TYPE = "client_credentials";

    @Bean
    public Keycloak run() {
        try {
            log.info("Attempting to connect to Keycloak at {}", SERVER_URL);

            Keycloak keycloak = KeycloakBuilder.builder().serverUrl(SERVER_URL).realm(REALM).grantType(GRANT_TYPE).clientId(CLIENT_ID).clientSecret(CLIENT_SECRET).build();

            AccessTokenResponse tokenResponse = keycloak.tokenManager().getAccessToken();
            log.info("Successfully connected to Keycloak. Access Token: {}", tokenResponse.getToken());

            return keycloak;
        } catch (Exception e) {
            log.error("Failed to connect to Keycloak", e);
            throw new RuntimeException("Could not connect to Keycloak", e);
        }
    }
}
