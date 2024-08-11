package com.image.minifier.main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Table(name = "users")
@NoArgsConstructor
@Entity
public class User {
    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "username")
    private String username;
    @Column(name = "password")
    private String password;
    @Column(name = "apiKey")
    private String apiKey;
    @Column(name = "email")
    private String email;
    @Column(name = "created_on")
    private Date createdOn;
    @Column(name = "last_login")
    private Date lastLogin;

    public User(String username, String password, String apiKey, String email, Date createdOn, Date lastLogin) {
        this.username = username;
        this.password = password;
        this.apiKey = apiKey;
        this.email = email;
        this.createdOn = createdOn;
        this.lastLogin = lastLogin;
    }


}
