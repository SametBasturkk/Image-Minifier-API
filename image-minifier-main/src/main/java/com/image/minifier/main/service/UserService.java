package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserService {

    private KeycloakConfig keycloak;

    @Value("${keycloak.realm}")
    public String REALM;

    public UserService(KeycloakConfig keycloak) {
        this.keycloak = keycloak;
    }


    public void createUser(CreateUserRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        Response response = keycloak.userResource().create(user);
        log.info("User {} created", request.getUsername());
        if (response.getStatus() != 201) {
            log.error("Failed to create user. Status code: {}, Response body: {}", response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException("Failed to create user");
        }
        response.close();
    }

    public void deleteUser(String username) {
        keycloak.userResource().get(username).remove();
        log.info("User {} deleted", username);
    }

    public void updateUser(String username, CreateUserRequest request) {
        UserRepresentation user = keycloak.run().realm(REALM).users().search(username).get(0);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        keycloak.run().realm(REALM).users().get(user.getId()).update(user);
        log.info("User {} updated", username);
        Response response = keycloak.userResource().create(user);
        if (response.getStatus() != 204) {
            log.error("Failed to update user. Status code: {}, Response body: {}", response.getStatus(), response.readEntity(String.class));
            throw new RuntimeException("Failed to update user");
        }
    }

    public List<UserRepresentation> getUsers() {
        List<UserRepresentation> listOfUsers = keycloak.run().realm(REALM).users().list();
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


}
