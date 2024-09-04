package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import com.image.minifier.main.model.User;
import com.image.minifier.main.repository.UserRepository;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.TokenVerifier;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class UserService {

    private final KeycloakConfig keycloak;
    private final UserRepository userRepository;

    public UserService(KeycloakConfig keycloak, UserRepository userRepository) {
        this.keycloak = keycloak;
        this.userRepository = userRepository;
    }

    public void createUser(CreateUserRequest request) {
        log.info("Attempting to create user: {}", request.getUsername());
        UserRepresentation user = buildUserRepresentation(request);
        Response response = keycloak.userResource().create(user);
        handleResponse(response, "User created successfully", "Failed to create user");
        updatePlanRole(request.getUsername(), "basic");
        userRepository.save(
                new User(request.getUsername(), 0, 0, 0, new Date())
        );
    }

    public void deleteUser(String username) {
        log.info("Attempting to delete user: {}", username);
        UserResource userResource = getUserResource(username);
        userResource.remove();
        log.info("User deleted successfully: {}", username);
    }

    public void updateUser(CreateUserRequest request) {
        log.info("Attempting to update user: {}", request.getUsername());
        UserResource userResource = getUserResource(request.getUsername());
        UserRepresentation user = buildUserRepresentation(request);
        userResource.update(user);
        log.info("User updated successfully: {}", request.getUsername());
    }

    public List<UserRepresentation> getUsers() {
        log.info("Retrieving all users");
        List<UserRepresentation> users = keycloak.userResource().list();
        log.info("Retrieved {} users", users.size());
        return users;
    }

    public UserRepresentation getUser(String username) {
        log.info("Retrieving user: {}", username);
        return getUserResource(username).toRepresentation();
    }

    public void updateApiKey(String username) {
        log.info("Updating API key for user: {}", username);
        UserResource userResource = getUserResource(username);
        updateAttribute(userResource, "api_key", generateApiKey());
        log.info("API key updated successfully for user: {}", username);
    }

    public void updatePlanRole(String username, String plan) {
        log.info("Updating plan role for user: {} to plan: {}", username, plan);
        UserResource userResource = getUserResource(username);
        userResource.roles().realmLevel().add(Collections.singletonList(keycloak.rolesResource().get(plan).toRepresentation()));
        log.info("Plan role updated successfully for user: {}", username);
    }

    public String userLogin(String username, String password) {
        log.info("Attempting login for user: {}", username);
        User user = userRepository.findByUsername(username);
        if (user == null) {
            log.error("User {} not found", username);
            throw new RuntimeException("User not found");
        }
        user.setLastLogin(new Date());
        try {
            Keycloak keycloakClient = Keycloak.getInstance(
                    keycloak.getSERVER_URL(),
                    keycloak.getREALM(),
                    username,
                    password,
                    keycloak.getCLIENT_ID(),
                    keycloak.getCLIENT_SECRET()
            );
            AccessTokenResponse tokenResponse = keycloakClient.tokenManager().getAccessToken();
            log.info("User {} logged in successfully", username);
            userRepository.save(user);
            return tokenResponse.getToken();
        } catch (Exception e) {
            log.error("Login failed for user {}: {}", username, e.getMessage());
            throw new RuntimeException("Login failed", e);
        }
    }

    public UserRepresentation getUserFromToken(String token) {
        log.info("Retrieving user by token");
        AccessToken accessToken = parseToken(token);
        String username = accessToken.getPreferredUsername();
        return getUser(username);
    }

    public String getApiKey(String token) {
        log.info("Retrieving API key for token");
        UserRepresentation user = getUserFromToken(token);
        return getUserAttribute(user, "api_key");
    }

    public void validateToken(String token) {
        log.info("Validating token");
        try {
            getUserFromToken(token);
            log.info("Token is valid");
        } catch (Exception e) {
            log.error("Invalid token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }

    public String validateApiKey(String apiKey, String token) {
        log.info("Validating API key");
        UserRepresentation user = getUserFromToken(token);
        if (!apiKey.equals(getUserAttribute(user, "api_key"))) {
            log.error("Invalid API key");
            throw new RuntimeException("Invalid API key");
        }
        log.info("API key is valid");
        return user.getUsername();
    }

    private UserRepresentation buildUserRepresentation(CreateUserRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setCredentials(Collections.singletonList(createCredential(request.getPassword())));
        user.setAttributes(Map.of("api_key", List.of(generateApiKey())));
        return user;
    }

    private CredentialRepresentation createCredential(String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }

    private String generateApiKey() {
        return UUID.randomUUID().toString();
    }

    private void updateAttribute(UserResource userResource, String key, String value) {
        UserRepresentation user = userResource.toRepresentation();
        user.setAttributes(Map.of(key, List.of(value)));
        userResource.update(user);
    }

    private String getUserAttribute(UserRepresentation user, String attributeName) {
        if (user.getAttributes() == null || !user.getAttributes().containsKey(attributeName)) {
            throw new RuntimeException(attributeName + " not found for user");
        }
        return user.getAttributes().get(attributeName).get(0);
    }

    private AccessToken parseToken(String token) {
        try {
            TokenVerifier<AccessToken> verifier = TokenVerifier.create(token, AccessToken.class);
            verifier.withChecks(TokenVerifier.IS_ACTIVE);
            return verifier.getToken();
        } catch (VerificationException e) {
            log.error("Token validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        } catch (Exception e) {
            log.error("Failed to parse token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }


    private UserResource getUserResource(String username) {
        List<UserRepresentation> users = keycloak.userResource().search(username);
        if (users.isEmpty()) {
            log.error("User {} not found", username);
            throw new RuntimeException("User not found");
        }
        return keycloak.userResource().get(users.get(0).getId());
    }

    private void handleResponse(Response response, String successMessage, String errorMessage) {
        if (response.getStatus() == 201 || response.getStatus() == 204) {
            log.info(successMessage);
        } else {
            log.error("{} Status code: {}, Response body: {}", errorMessage, response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException(errorMessage);
        }
        response.close();
    }
}
