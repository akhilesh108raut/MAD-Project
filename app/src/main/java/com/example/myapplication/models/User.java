package com.example.myapplication.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity for User.
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    private String userId;
    private String name;
    private String username;
    private String email;
    private String phone;
    private String password; // Added for local auth
    private long createdAt;

    public User() {}

    public User(@NonNull String userId, String name, String username, String email, String phone, String password) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    @NonNull
    public String getUserId() { return userId; }
    public void setUserId(@NonNull String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
