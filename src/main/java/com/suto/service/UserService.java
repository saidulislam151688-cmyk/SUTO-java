package com.suto.service;

import com.suto.model.User;
import com.suto.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<User> getAllDrivers() {
        return userRepository.findByRole("driver");
    }

    public User getUserById(String id) {
        if ("me".equals(id)) {
            // Should resolve from token context, but for now specific ID required
            return null;
        }
        return userRepository.findById(UUID.fromString(id)).orElse(null);
    }
}
