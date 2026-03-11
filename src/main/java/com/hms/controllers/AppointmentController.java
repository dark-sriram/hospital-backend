package com.hms.controllers;

import com.hms.models.Appointment;
import com.hms.repositories.AppointmentRepository;
import com.hms.services.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private AppointmentRepository appointmentRepository;

    @PostMapping("/book")
    public ResponseEntity<?> book(@RequestBody Appointment appointment) {
        try {
            // In a real app, set patientId from SecurityContext
            return ResponseEntity.ok(appointmentService.bookAppointment(appointment));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    @GetMapping
    public List<Appointment> getAppointments() {
        return appointmentRepository.findAll();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Appointment app = appointmentRepository.findById(id).orElse(null);
        if (app == null)
            return ResponseEntity.notFound().build();
        app.setStatus(Appointment.Status.valueOf(body.get("status")));
        return ResponseEntity.ok(appointmentRepository.save(app));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        // Simplified reporting for now
        return ResponseEntity.ok(Map.of("perDoctor", List.of(), "revenue", List.of()));
    }
}
