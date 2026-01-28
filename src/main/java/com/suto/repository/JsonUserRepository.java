package com.suto.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.suto.model.User;
import com.suto.util.JsonFileService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class JsonUserRepository implements UserRepository {

    private static final String USERS_FILE = "users.json";
    private final JsonFileService jsonFileService;

    // Constructor with dependency injection
    public JsonUserRepository(JsonFileService jsonFileService) {
        this.jsonFileService = jsonFileService;
    }

    // Default constructor for backward compatibility
    public JsonUserRepository() {
        this.jsonFileService = new JsonFileService();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> users = getAllUsers();
        return users.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public User save(User user) {
        List<User> users = getAllUsers();

        // Check if user already exists (update)
        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                user.setUpdatedAt(LocalDateTime.now());
                users.set(i, user);
                updated = true;
                break;
            }
        }

        // If not updated, add as new user
        if (!updated) {
            if (user.getId() == null) {
                user.setId(UUID.randomUUID());
            }
            if (user.getCreatedAt() == null) {
                user.setCreatedAt(LocalDateTime.now());
            }
            users.add(user);
        }

        jsonFileService.writeList(USERS_FILE, users);
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        List<User> users = getAllUsers();
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<User> findByRole(String role) {
        List<User> users = getAllUsers();
        return users.stream()
                .filter(u -> role.equalsIgnoreCase(u.getRole()))
                .collect(Collectors.toList());
    }

    private List<User> getAllUsers() {
        return jsonFileService.readList(USERS_FILE, new TypeReference<List<User>>() {
        });
    }
}
