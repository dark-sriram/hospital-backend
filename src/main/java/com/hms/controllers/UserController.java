package com.hms.controllers;

import com.hms.models.User;
import com.hms.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/doctors")
    public List<User> getDoctors(@RequestParam(required = false) String specialization) {
        if (specialization != null && !specialization.isEmpty()) {
            return userRepository.findByRoleAndSpecializationContainingIgnoreCase(User.Role.DOCTOR, specialization);
        }
        return userRepository.findByRole(User.Role.DOCTOR);
    }

    @PutMapping("/availability")
    public ResponseEntity<?> updateAvailability(@RequestBody Map<String, List<User.Slot>> body) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(Map.of("msg", "User not found"));
        
        user.setAvailableSlots(body.get("slots"));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("msg", "Availability updated"));
    }
}
