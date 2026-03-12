package com.hms.services;

import com.hms.models.Appointment;
import com.hms.models.User;
import com.hms.repositories.AppointmentRepository;
import com.hms.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private UserRepository userRepository;

    public Appointment bookAppointment(Appointment appointment) throws Exception {
        System.out.println("--- Service: Starting bookAppointment ---");
        System.out.println("Searching for doctor ID: " + appointment.getDoctorId());
        
        User doctor = userRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new Exception("Doctor ID [" + appointment.getDoctorId() + "] not found in database"));
        
        System.out.println("Doctor found: " + doctor.getName());
        
        if (doctor.getAvailableSlots() == null || doctor.getAvailableSlots().isEmpty()) {
            throw new Exception("Doctor " + doctor.getName() + " has no available slots scheduled");
        }

        System.out.println("Requested slot: Date=" + appointment.getAppointmentDate() + ", Start=" + appointment.getStartTime() + ", End=" + appointment.getEndTime());

        // Logic check: verify if the requested slot EXISTS in doctor's availability
        boolean isSlotValid = doctor.getAvailableSlots().stream()
                .anyMatch(slot -> {
                    boolean dMatch = slot.getDate().equals(appointment.getAppointmentDate());
                    boolean sMatch = slot.getStartTime().equals(appointment.getStartTime());
                    boolean eMatch = slot.getEndTime().equals(appointment.getEndTime());
                    return dMatch && sMatch && eMatch;
                });
        
        if (!isSlotValid) {
            System.out.println("FAILED: Slot mismatch. Requested: " + appointment.getAppointmentDate() + " " + appointment.getStartTime());
            System.out.println("Doctor available slots count: " + doctor.getAvailableSlots().size());
            throw new Exception("The selected time slot does not match the doctor's published availability");
        }

        // Overlap Check
        System.out.println("Checking for overlaps...");
        if (hasOverlap(appointment.getDoctorId(), appointment.getPatientId(),
                appointment.getAppointmentDate(), appointment.getStartTime(), appointment.getEndTime())) {
            System.out.println("FAILED: Overlap detected");
            throw new Exception("This time slot is already booked");
        }

        System.out.println("Saving appointment to repository...");
        Appointment saved = appointmentRepository.save(appointment);
        System.out.println("Saved! ID: " + saved.getId());
        return saved;
    }

    private boolean hasOverlap(String docId, String patId, String date, String start, String end) {
        List<Appointment> docAppts = appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusNot(docId, date,
                Appointment.Status.CANCELLED);
        boolean docOverlap = docAppts.stream()
                .anyMatch(a -> isOverlapping(start, end, a.getStartTime(), a.getEndTime()));
        if (docOverlap)
            return true;

        List<Appointment> patAppts = appointmentRepository.findByPatientIdAndAppointmentDateAndStatusNot(patId, date,
                Appointment.Status.CANCELLED);
        return patAppts.stream().anyMatch(a -> isOverlapping(start, end, a.getStartTime(), a.getEndTime()));
    }

    private boolean isOverlapping(String s1, String e1, String s2, String e2) {
        return s1.compareTo(e2) < 0 && s2.compareTo(e1) < 0;
    }
}
