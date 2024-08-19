package com.image.minifier.main.service;

import com.image.minifier.main.configuration.KeycloakConfig;
import com.image.minifier.main.dto.CreateUserRequest;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.image.minifier.main.configuration.KeycloakConfig.REALM;

@Service
public class UserService {

    private KeycloakConfig keycloak;

    public UserService(KeycloakConfig keycloak) {
        this.keycloak = keycloak;
    }

    public void createUser(CreateUserRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setCredentials(createCredentialRepresentation(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setEnabled(true);
        keycloak.run().realm(REALM).users().create(user);
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
