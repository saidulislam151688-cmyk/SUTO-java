package com.suto.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abstract Base Entity - Demonstrates Inheritance & Abstraction
 * Common fields and methods for all domain entities
 */
public abstract class BaseEntity {

    // Protected fields - accessible by child classes
    protected UUID id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    // Default constructor
    public BaseEntity() {
    }

    // Parameterized constructor
    public BaseEntity(UUID id, LocalDateTime createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    // Common getters - inherited by all entities
    public UUID getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Common setters - inherited by all entities
    public void setId(UUID id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Abstract method - forces child classes to implement
     * Demonstrates Abstraction & Polymorphism
     */
    public abstract String getEntityName();

    @Override
    public String toString() {
        return getEntityName() + "{id=" + id + ", createdAt=" + createdAt + "}";
    }
}
