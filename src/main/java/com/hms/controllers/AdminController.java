package com.hms.controllers;

import com.hms.models.User;
import com.hms.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/add-doctor")
    public ResponseEntity<?> addDoctor(@RequestBody User doctor) {
        if (userRepository.findByEmail(doctor.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("msg", "Doctor already exists"));
        }
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        doctor.setRole(User.Role.DOCTOR);
        User savedDoctor = userRepository.save(doctor);
        return ResponseEntity.ok(savedDoctor);
    }
}
