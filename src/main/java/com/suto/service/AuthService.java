package com.suto.service;

import com.suto.dto.AuthResponse;
import com.suto.dto.LoginRequest;
import com.suto.dto.SignupRequest;
import com.suto.model.User;
import com.suto.repository.UserRepository;
import com.suto.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthResponse signup(SignupRequest request) {
        // Check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setId(UUID.randomUUID()); // Generate our own ID since we detached from auth.users (or managing it
                                       // ourselves)
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setRole(request.getRole() != null ? request.getRole() : "passenger");

        // Save to DB
        userRepository.save(user);

        // Generate Token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId().toString());

        return new AuthResponse(token, user.getRole(), "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId().toString());
        return new AuthResponse(token, user.getRole(), "Login successful");
    }

    public User getUserProfile(String token) {
        String userIdStr = jwtUtil.extractClaim(token.replace("Bearer ", ""),
                claims -> claims.get("userId", String.class));
        return userRepository.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
