package com.suto.dto;

public class SignupRequest {
    private String name;
    private String email;
    private String password;
    private String role; // Optional: "passenger", "driver", etc.

    // Default constructor
    public SignupRequest() {
    }

    // All-args constructor
    public SignupRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getFullName() {
        return name; // Alias for backward compatibility
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setFullName(String name) {
        this.name = name; // Alias for backward compatibility
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
