package com.hms.controllers;

import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.repositories.AppointmentRepository;
import com.hms.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DiagnosticController {
    @Autowired private UserRepository userRepository;
    @Autowired private AppointmentRepository appointmentRepository;

    @GetMapping("/inspect")
    public Map<String, Object> inspect() {
        return Map.of(
            "users", userRepository.findAll(),
            "appointments", appointmentRepository.findAll()
        );
    }
}
