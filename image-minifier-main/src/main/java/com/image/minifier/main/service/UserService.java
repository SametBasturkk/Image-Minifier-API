package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
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
        log.info("Attempting to create user: {}", request.getUsername());
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
        Response response = keycloak.userResource().create(user);
        if (response.getStatus() == 201) {
            log.info("User {} created successfully", request.getUsername());
        } else {
            log.error("Failed to create user {}. Status code: {}, Response body: {}", request.getUsername(), response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException("Failed to create user");
        }
        updatePlanRole(request.getUsername(), "basic");
        response.close();
    }

    public void deleteUser(String username) {
        log.info("Attempting to delete user: {}", username);
        try {
            UsersResource usersResource = keycloak.userResource();
            List<UserRepresentation> users = usersResource.search(username, true);

            if (users.isEmpty()) {
                log.error("User {} not found", username);
                throw new RuntimeException("User not found");
            }

            UserRepresentation user = users.get(0);
            if (!username.equals(user.getUsername())) {
                log.error("User {} not found", username);
                throw new RuntimeException("User not found");
            }

            Response response = usersResource.delete(user.getId());
            if (response.getStatus() != 204) {
                log.error("Failed to delete user {}. Status: {}", username, response.getStatus());
                throw new RuntimeException("Failed to delete user");
            }

            log.info("User {} deleted successfully", username);
        } catch (Exception e) {
            log.error("Error deleting user {}: {}", username, e.getMessage());
            throw new RuntimeException("Error deleting user", e);
        }
    }

    public void updateUser(CreateUserRequest request) {
        log.info("Attempting to update user: {}", request.getUsername());
        UserRepresentation user = keycloak.userResource().search(request.getUsername()).get(0);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        keycloak.userResource().get(user.getId()).update(user);

    }

    public List<UserRepresentation> getUsers() {
        log.info("Retrieving all users");
        List<UserRepresentation> listOfUsers = keycloak.userResource().list();
        log.info("Retrieved {} users", listOfUsers.size());
        return listOfUsers;
    }

    private List<CredentialRepresentation> createCredentialRepresentation(String password) {
        List<CredentialRepresentation> resp = new ArrayList<>();
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(false);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(password);
        resp.add(passwordCred);
        return resp;
    }

    public UserRepresentation getUser(String username) {
        log.info("Retrieving user: {}", username);
        List<UserRepresentation> users = keycloak.userResource().search(username);
        if (users.isEmpty()) {
            log.error("User {} not found", username);
            throw new RuntimeException("User not found");
        }
        log.info("User {} retrieved successfully", username);
        return users.get(0);
    }

    public void updateApiKey(String username) {
        log.info("Updating API key for user: {}", username);
        UserRepresentation user = getUser(username);
        HashMap<String, List<String>> attributes = new HashMap<>();
        attributes.put("api_key", List.of(UUID.randomUUID().toString()));
        user.setAttributes(attributes);
        keycloak.userResource().get(user.getId()).update(user);
        log.info("API key updated successfully for user {}", username);
    }

    public void updatePlanRole(String username, String plan) {
        log.info("Updating plan role for user: {} to plan: {}", username, plan);
        UserResource userResource = keycloak.userResource().get(getUser(username).getId());
        userResource.roles().realmLevel().add(List.of(keycloak.rolesResource().get(plan).toRepresentation()));
        log.info("Plan role updated successfully for user: {} to plan: {}", username, plan);
    }

    public String userLogin(String username, String password) {
        log.info("Attempting login for user: {}", username);
        try {
            Keycloak keycloakClient = Keycloak.getInstance(keycloak.getSERVER_URL(), keycloak.getREALM(), username, password, keycloak.getCLIENT_ID(), keycloak.getCLIENT_SECRET());
            AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
            if (tokenResponse != null && tokenResponse.getToken() != null) {
                log.info("User {} logged in successfully", username);
                return tokenResponse.getToken();
            } else {
                log.error("Login failed for user {}", username);
                throw new RuntimeException("Login failed");
            }
        } catch (Exception e) {
            log.error("Error during login for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Error during login", e);
        }
    }

    public UserRepresentation getUserFromToken(String token) throws VerificationException {
        log.info("Retrieving user by token");
        AccessToken accessToken = TokenVerifier.create(token, AccessToken.class).getToken();

        if (accessToken != null && accessToken.getPreferredUsername() != null) {
            log.info("User retrieved successfully by token");
            return getUser(accessToken.getPreferredUsername());
        } else {
            log.error("User not found for the given token");
            throw new RuntimeException("User not found");
        }
    }

    public String getApiKey(String token) throws VerificationException {
        log.info("Retrieving API key for token");
        UserRepresentation user = getUserFromToken(token);
        if (user != null && user.getAttributes() != null && user.getAttributes().containsKey("api_key")) {
            log.info("API key retrieved successfully");
            return user.getAttributes().get("api_key").get(0);
        } else {
            log.error("API key not found for the given token");
            return null;
        }
    }

    public void validateToken(String token) {
        log.info("Validating token");
        try {
            Keycloak keycloakClient = Keycloak.getInstance(keycloak.getSERVER_URL(), keycloak.getREALM(), token, keycloak.getCLIENT_ID(), keycloak.getCLIENT_SECRET());
            AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
            if (tokenResponse != null && tokenResponse.getToken() != null) {
                log.info("Token is valid");
            } else {
                log.error("Token is invalid");
                throw new RuntimeException("Invalid token");
            }
        } catch (Exception e) {
            log.error("Error during token validation: {}", e.getMessage());
            throw new RuntimeException("Error validating token", e);
        }
    }

    public void validateApiKey(String apiKey, String token) throws VerificationException {
        log.info("Validating API key");
        UserRepresentation user = getUserFromToken(token);
        if (user == null) {
            log.error("User not found for the given token");
            throw new RuntimeException("User not found");
        }
        List<String> apiKeys = user.getAttributes().get("api_key");
        if (apiKeys != null && apiKeys.contains(apiKey)) {
            log.info("API key is valid");
        } else {
            log.error("Invalid API key");
            throw new RuntimeException("Invalid API key");
        }
    }
}