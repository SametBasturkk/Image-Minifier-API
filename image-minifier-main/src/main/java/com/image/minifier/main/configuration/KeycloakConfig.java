package com.image.minifier.main.configuration;

import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    public static final String REALM = "minifier";
    public static final String SERVER_URL = "http://185.136.206.146:8081/auth";
    public static final String CLIENT_ID = "minifyClient";
    public static final String CLIENT_SECRET = "NRrDvzqGuzxrSbP8PfWEkEtm9NNejK0b";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "admin";

    private static Keycloak keycloakInstance = null;

    @Bean
    public Keycloak getInstance() {
        if (keycloakInstance == null) {
            keycloakInstance = Keycloak.getInstance(SERVER_URL, REALM, USERNAME, PASSWORD, CLIENT_ID, CLIENT_SECRET);
        }
        return keycloakInstance;
    }
}