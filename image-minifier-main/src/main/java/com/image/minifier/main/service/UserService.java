package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
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
        keycloak.run().realm(REALM).users().create(user);
    }

    public void deleteUser(String username) {
        keycloak.run().realm(REALM).users().delete(username);
    }

    public void updateUser(String username, CreateUserRequest request) {
        UserRepresentation user = keycloak.run().realm(REALM).users().search(username).get(0);
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        keycloak.run().realm(REALM).users().get(user.getId()).update(user);
    }

    public List<UserRepresentation> getUsers() {
        return keycloak.run().realm(REALM).users().list();
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


}
