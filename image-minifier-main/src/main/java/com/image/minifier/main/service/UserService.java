package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.resource.UsersResource;
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
        UsersResource userResource = keycloak.run().realm(REALM).users();
        Response response = userResource.create(user);
        log.info("User {} created", request.getUsername());
        log.info("Response status: {}", response.getStatus());
        response.close();
    }

    public void deleteUser(String username) {
        keycloak.run().realm(REALM).users().delete(username);
        log.info("User {} deleted", username);
    }

    public void updateUser(String username, CreateUserRequest request) {
        UserRepresentation user = keycloak.run().realm(REALM).users().search(username).get(0);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        keycloak.run().realm(REALM).users().get(user.getId()).update(user);
        log.info("User {} updated", username);
    }

    public List<UserRepresentation> getUsers() {
        List<UserRepresentation> listOfUsers = keycloak.run().realm(REALM).users().list();
        log.info("List of users: {}", listOfUsers);
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
