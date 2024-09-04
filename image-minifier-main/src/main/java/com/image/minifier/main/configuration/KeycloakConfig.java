package com.image.minifier.main.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
@Getter
public class KeycloakConfig {


    @Value("${keycloak.realm}")
    public String REALM;

    @Value("${keycloak.auth-server-url}")
    public String SERVER_URL;

    @Value("${keycloak.resource}")
    public String CLIENT_ID;

    @Value("${keycloak.credentials.secret}")
    public String CLIENT_SECRET;

    @Value("${keycloak.grant-type}")
    public String GRANT_TYPE;

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

    @Bean
    public UsersResource userResource() {
        return run().realm(REALM).users();
    }

    @Bean
    public RolesResource rolesResource() {
        return run().realm(REALM).roles();
    }



}
