package com.suto.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * User Entity
 * Demonstrates:
 * 1. Inheritance (extends BaseEntity)
 * 2. Encapsulation (private fields)
 * 3. Polymorphism (overrides getEntityName)
 */
public class User extends BaseEntity {

    // User-specific fields only (id, createdAt, updatedAt are inherited)
    private String name;
    private String email;
    private String password; // For JSON serialization
    private String passwordHash; // For security
    private String role; // "passenger", "driver", "admin"

    // Default constructor
    public User() {
        super(); // Call parent constructor
    }

    // Parameterized constructor
    public User(UUID id, String name, String email, String password, LocalDateTime createdAt) {
        super(id, createdAt); // Initialize parent fields
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Alias for backward compatibility if needed
    public String getFullName() {
        return name;
    }

    public void setFullName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Polymorphism Example: Overriding abstract method
     */
    @Override
    public String getEntityName() {
        return "User";
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
