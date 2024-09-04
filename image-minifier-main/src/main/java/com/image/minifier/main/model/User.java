package com.image.minifier.main.model;

import jakarta.persistence.*;
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
    @GeneratedValue
    private long id;
    @Column(name = "username")
    private String username;
    @Column(name = "total_images_processed")
    private long totalImagesProcessed;
    @Column(name = "total_bytes_saved")
    private long totalBytesSaved;
    @Column(name = "total_bytes_processed")
    private long totalBytesProcessed;
    @Column(name = "created_on")
    private Date createdOn = new Date();
    @Column(name = "last_login")
    private Date lastLogin;

    public User(String username, long totalImagesProcessed, long totalBytesSaved, long totalBytesProcessed, Date lastLogin) {
        this.username = username;
        this.totalImagesProcessed = totalImagesProcessed;
        this.totalBytesSaved = totalBytesSaved;
        this.totalBytesProcessed = totalBytesProcessed;
        this.lastLogin = lastLogin;
    }


}
