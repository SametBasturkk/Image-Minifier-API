package com.image.minifier.main.configuration;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    public static final String REALM = "minifier";
    public static final String SERVER_URL = "http://185.136.206.146:8081/auth";
    public static final String CLIENT_ID = "admin-cli";
    public static final String CLIENT_SECRET = "TRXOHE4LbDRHtgoPMxVFBqBvT2dwhCZy";
    public static final String GRANT_TYPE = "client_credentials";


    @Bean
    public Keycloak run() {
        return KeycloakBuilder.builder()
                .serverUrl(SERVER_URL)
                .realm(REALM)
                .grantType(GRANT_TYPE)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .build();
    }
}