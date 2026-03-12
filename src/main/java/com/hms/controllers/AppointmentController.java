package com.hms.controllers;

import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.repositories.AppointmentRepository;
import com.hms.repositories.UserRepository;
import com.hms.services.AppointmentService;
import com.hms.dto.AppointmentResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {
    @Autowired private AppointmentService appointmentService;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;

    @PostMapping("/book")
    public ResponseEntity<?> book(@RequestBody Appointment appointment) {
        System.out.println(">>> Incoming Book Request Body: " + appointment);
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            System.out.println(">>> Principal: " + principal + " (Type: " + principal.getClass().getName() + ")");
            String email = principal.toString();
            
            User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User with email " + email + " not found in DB"));
            
            System.out.println(">>> Found Patient: " + patient.getName() + " [ID=" + patient.getId() + "]");
            
            if (patient.getId() == null) {
                System.err.println("CRITICAL: Patient ID is null for user " + email);
            }

            appointment.setPatientId(patient.getId());
            appointment.setStatus(Appointment.Status.BOOKED); // Default status
            
            System.out.println(">>> Appointment object ready for service: " + appointment);
            Appointment saved = appointmentService.bookAppointment(appointment);
            System.out.println(">>> SUCCESS: Appointment saved with ID " + saved.getId());
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println(">>> BOOKING FAILED: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("msg", e.getMessage()));
        }
    }

    @GetMapping
    public List<AppointmentResponse> getAppointments() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return List.of();
        
        List<Appointment> apps;
        if (user.getRole() == User.Role.DOCTOR) {
            apps = appointmentRepository.findByDoctorIdAndStatusNot(user.getId(), Appointment.Status.CANCELLED);
        } else if (user.getRole() == User.Role.PATIENT) {
            apps = appointmentRepository.findByPatientIdAndStatusNot(user.getId(), Appointment.Status.CANCELLED);
        } else {
            apps = appointmentRepository.findAll();
        }

        return apps.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AppointmentResponse mapToResponse(Appointment app) {
        AppointmentResponse res = new AppointmentResponse();
        res.setId(app.getId());
        res.setAppointmentDate(app.getAppointmentDate());
        res.setStartTime(app.getStartTime());
        res.setEndTime(app.getEndTime());
        res.setStatus(app.getStatus());

        if (app.getPatientId() != null) {
            userRepository.findById(app.getPatientId()).ifPresent(u -> {
                AppointmentResponse.UserShort ps = new AppointmentResponse.UserShort();
                ps.setId(u.getId());
                ps.setName(u.getName());
                res.setPatientId(ps);
            });
        }

        if (app.getDoctorId() != null) {
            userRepository.findById(app.getDoctorId()).ifPresent(u -> {
                AppointmentResponse.UserShort ds = new AppointmentResponse.UserShort();
                ds.setId(u.getId());
                ds.setName(u.getName());
                res.setDoctorId(ds);
            });
        }

        return res;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable String id, @RequestBody Map<String, String> body) {
        Appointment app = appointmentRepository.findById(id).orElse(null);
        if (app == null) return ResponseEntity.notFound().build();
        try {
            app.setStatus(Appointment.Status.valueOf(body.get("status")));
            return ResponseEntity.ok(appointmentRepository.save(app));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("msg", "Invalid status"));
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<?> getReports() {
        try {
            List<Appointment> all = appointmentRepository.findAll();
            
            Map<String, Long> counts = all.stream()
                .filter(a -> a.getDoctorId() != null)
                .collect(Collectors.groupingBy(Appointment::getDoctorId, Collectors.counting()));
            
            List<Map<String, Object>> perDoctor = counts.entrySet().stream().map(e -> {
                String name = userRepository.findById(e.getKey()).map(User::getName).orElse("Unknown");
                Map<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("count", e.getValue());
                return map;
            }).collect(Collectors.toList());

            Map<String, Long> revMap = all.stream()
                .filter(a -> a.getStatus() == Appointment.Status.COMPLETED && a.getDoctorId() != null)
                .collect(Collectors.groupingBy(a -> {
                    return userRepository.findById(a.getDoctorId()).map(User::getSpecialization).orElse("General");
                }, Collectors.counting()));

            List<Map<String, Object>> revenue = revMap.entrySet().stream().map(e -> {
                Map<String, Object> map = new HashMap<>();
                map.put("_id", e.getKey());
                map.put("revenue", e.getValue() * 100);
                return map;
            }).collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("perDoctor", perDoctor);
            response.put("revenue", revenue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("msg", e.getMessage()));
        }
    }
}
