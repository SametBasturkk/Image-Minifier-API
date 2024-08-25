package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    private final KeycloakConfig keycloak;

    public UserService(KeycloakConfig keycloak) {
        this.keycloak = keycloak;
    }


    public void createUser(CreateUserRequest request) {
        HashMap<String, List<String>> attributes = new HashMap<>();
        attributes.put("api_key", List.of(UUID.randomUUID().toString()));
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setAttributes(attributes);
        user.setRealmRoles(List.of("basic"));
        Response response = keycloak.userResource().create(user);
        log.info("User {} created", request.getUsername());
        if (response.getStatus() != 201) {
            log.error("Failed to create user. Status code: {}, Response body: {}", response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException("Failed to create user");
        }
        response.close();
    }

    public void deleteUser(String username) {
        UserResource test = keycloak.userResource().get(username);
        log.info("Attempting to delete user {}", test.toRepresentation().getUsername());
        if (username == test.toRepresentation().getUsername()) {
            log.error("User {} not found", username);
            throw new RuntimeException("User not found");
        }
        test.remove();
        log.info("User {} deleted", username);
    }

    public void updateUser(CreateUserRequest request) {
        UserRepresentation user = keycloak.run().realm(keycloak.getREALM()).users().search(request.getUsername()).get(0);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        keycloak.run().realm(keycloak.getREALM()).users().get(user.getId()).update(user);
        log.info("User {} updated", request.getUsername());
        Response response = keycloak.userResource().create(user);
        if (response.getStatus() != 204) {
            log.error("Failed to update user. Status code: {}, Response body: {}", response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException("Failed to update user");
        }
    }

    public List<UserRepresentation> getUsers() {
        List<UserRepresentation> listOfUsers = keycloak.run().realm(keycloak.getREALM()).users().list();
        log.info("Users retrieved");
        return listOfUsers;
    }


    private List<CredentialRepresentation> createCredentialRepresentation(String password) {
        List<CredentialRepresentation> resp = new ArrayList<>();
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        resp.add(passwordCred);
        log.info("Credential representation created");
        return resp;
    }

    public UserRepresentation getUser(String username) {
        UserRepresentation user = keycloak.run().realm(keycloak.getREALM()).users().search(username).get(0);
        log.info("User {} retrieved", username);
        return user;
    }

    public void updateApiKey(String username) {
        UserRepresentation user = keycloak.run().realm(keycloak.getREALM()).users().search(username).get(0);
        HashMap<String, List<String>> attributes = new HashMap<>();
        attributes.put("api_key", List.of(UUID.randomUUID().toString()));
        user.setAttributes(attributes);
        keycloak.run().realm(keycloak.getREALM()).users().get(user.getId()).update(user);
        log.info("API key updated for user {}", username);
    }

    public void updatePlanRole(String username, String plan) {
        UserRepresentation user = keycloak.run().realm(keycloak.getREALM()).users().search(username).get(0);
        user.setRealmRoles(List.of(plan));
        keycloak.run().realm(keycloak.getREALM()).users().get(user.getId()).update(user);
        log.info("Plan role updated for user {}", username);
    }

    public String userLogin(String username, String password) {
        try {
            Keycloak keycloakClient = Keycloak.getInstance(keycloak.getSERVER_URL(), keycloak.getREALM(), username, password, keycloak.getCLIENT_ID());

            AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();

            if (tokenResponse != null && tokenResponse.getToken() != null) {
                log.info("User {} logged in successfully", username);
                return tokenResponse.getToken();
            } else {
                log.error("Login failed for user {}", username);
                return null;
            }
        } catch (Exception e) {
            log.error("Error during login for user {}: {}", username, e.getMessage());
            return null;
        }
    }

    public UserRepresentation getUserByToken(String token) {
        try {
            Keycloak keycloakClient = Keycloak.getInstance(keycloak.getSERVER_URL(), keycloak.getREALM(), token, keycloak.getCLIENT_ID());

            return keycloakClient.realm(keycloak.getREALM()).users().search(token).get(0);
        } catch (Exception e) {
            log.error("Error during getting user by token: {}", e.getMessage());
            return null;
        }
    }

    public String getApiKey(String token) {
        UserRepresentation user = getUserByToken(token);
        if (user != null) {
            return user.getAttributes().get("api_key").get(0);
        } else {
            return null;
        }
    }

    public void validateToken(String token) {
        try {
            Keycloak keycloakClient = Keycloak.getInstance(keycloak.getSERVER_URL(), keycloak.getREALM(), token, keycloak.getCLIENT_ID());

            AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();

            if (tokenResponse != null && tokenResponse.getToken() != null) {
                log.info("Token {} is valid", token);
            } else {
                log.error("Token {} is invalid", token);
            }
        } catch (Exception e) {
            log.error("Error during token validation for token {}: {}", token, e.getMessage());
        }
    }


    public void validateApiKey(String apiKey, String token) {
        UserRepresentation user = getUserByToken(token);
        List<String> attrib = user.getAttributes().get("api_key");
        if (attrib != null && attrib.contains(apiKey)) {
            log.info("API key {} is valid", apiKey);
        } else {
            log.error("API key {} is invalid", apiKey);
            throw new RuntimeException("Invalid API key");
        }
    }
}
