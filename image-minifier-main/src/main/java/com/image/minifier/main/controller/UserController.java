package com.image.minifier.main.controller;

import com.image.minifier.main.dto.CreateUserRequest;
import com.image.minifier.main.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        userService.createUser(request);
        log.info("User created successfully : {}", request.getUsername());
        return ResponseEntity.ok("User created successfully");
    }

    @PostMapping("/delete")
    public ResponseEntity<String> deleteUser(@RequestBody String username) {
        userService.deleteUser(username);
        log.info("User deleted successfully : {}", username);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateUser(@RequestBody CreateUserRequest request) {
        userService.updateUser(request);
        log.info("User updated successfully : {}", request.getUsername());
        return ResponseEntity.ok("User updated successfully");
    }

    @GetMapping("/get")
    public ResponseEntity<List<UserRepresentation>> getUsers() {
        return ResponseEntity.ok(userService.getUsers());
    }

    @PostMapping(value = "/login", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> login(@RequestParam("username") String username, @RequestParam("password") String password) {
        log.info("User logged in successfully : {}", userService.userLogin(username, password));
        return ResponseEntity.ok(userService.userLogin(username, password));
    }

    @GetMapping(value = "/get-api-key", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> getApiKey(@RequestParam("token") String token) {
        log.info("Received request to get api key for user: {}", token);
        log.info("Api key retrieved successfully : {}", userService.getApiKey(token));
        return ResponseEntity.ok(userService.getApiKey(token));
    }

}