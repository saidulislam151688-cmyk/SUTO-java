package com.suto.service;

import com.suto.dto.AuthResponse;
import com.suto.dto.LoginRequest;
import com.suto.dto.SignupRequest;
import com.suto.exception.DuplicateEmailException;
import com.suto.exception.InvalidCredentialsException;
import com.suto.exception.UserNotFoundException;
import com.suto.model.User;
import com.suto.repository.UserRepository;
import com.suto.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.UUID;

public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse signup(SignupRequest request) {
        // Check if email exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = new User();
        // ID generation is handled by repository if null
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
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId().toString());
        return new AuthResponse(token, user.getRole(), "Login successful");
    }

    public User getUserProfile(String token) {
        String userIdStr = jwtUtil.extractClaim(token.replace("Bearer ", ""),
                claims -> claims.get("userId", String.class));
        return userRepository.findById(UUID.fromString(userIdStr))
                .orElseThrow(() -> new UserNotFoundException("User ID: " + userIdStr));
    }
}
