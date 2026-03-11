package com.hms.controllers;

import com.hms.models.User;
import com.hms.repositories.UserRepository;
import com.hms.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "User already exists"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(User.Role.PATIENT); // Force PATIENT role on registration
        User savedUser = userRepository.save(user);
        String token = jwtUtils.generateToken(savedUser.getEmail(), savedUser.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "user", savedUser));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Hardcoded Admin Check
        if ("admin@medisync.com".equals(email) && "admin123".equals(password)) {
            String token = jwtUtils.generateToken(email, "ADMIN");
            Map<String, Object> adminUser = new HashMap<>();
            adminUser.put("name", "System Admin");
            adminUser.put("email", email);
            adminUser.put("role", "ADMIN");
            return ResponseEntity.ok(Map.of("token", token, "user", adminUser));
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("msg", "Invalid credentials"));
        }
        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(Map.of("token", token, "user", user));
    }
}
