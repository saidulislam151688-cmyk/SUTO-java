package com.suto.service;

import com.suto.exception.UserNotFoundException;
import com.suto.model.User;
import com.suto.repository.UserRepository;
import com.suto.security.JwtUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public List<User> getAllDrivers() {
        return userRepository.findByRole("driver");
    }

    public User updateUser(UUID id, User updatedUser) {
        User existingUser = getUserById(id);

        if (updatedUser.getFullName() != null) {
            existingUser.setFullName(updatedUser.getFullName());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }

        // Don't allow password or role update here for security

        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User ID: " + id));
    }

    public User getUserById(String id) {
        if ("me".equals(id)) {
            // Should resolve from token context, but for now specific ID required
            return null;
        }
        return userRepository.findById(UUID.fromString(id)).orElse(null);
    }

    public User getProfile(String token) {
        String userIdStr = jwtUtil.extractClaim(token.replace("Bearer ", ""),
                claims -> claims.get("userId", String.class));
        return userRepository.findById(UUID.fromString(userIdStr)).orElse(null);
    }
}
