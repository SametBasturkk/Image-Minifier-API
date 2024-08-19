package com.image.minifier.main.controller;

import com.image.minifier.main.dto.CreateUserRequest;
import com.image.minifier.main.service.UserService;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody String username) {
        userService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody String username, @RequestBody CreateUserRequest request) {
        userService.updateUser(username, request);
        return ResponseEntity.ok("User updated successfully");
    }

    @PostMapping("/get")
    public ResponseEntity<List<UserRepresentation>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

}