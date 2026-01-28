package com.suto.repository;

import com.suto.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for User entity operations.
 * Implementation: JsonUserRepository (JSON file storage)
 */
public interface UserRepository {
    Optional<User> findByEmail(String email);

    User save(User user);

    Optional<User> findById(UUID id);

    java.util.List<User> findByRole(String role);
}
