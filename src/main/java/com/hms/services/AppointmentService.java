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
        System.out.println("--- Service: Booking for Doctor ID: " + appointment.getDoctorId());
        
        User doctor = userRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new Exception("Doctor not found"));
        
        if (doctor.getAvailableSlots() == null || doctor.getAvailableSlots().isEmpty()) {
            throw new Exception("Doctor has no available slots scheduled");
        }

        // Lenient match: Date and StartTime are enough to identify the slot
        boolean isSlotValid = doctor.getAvailableSlots().stream()
                .anyMatch(slot -> 
                    slot.getDate() != null && slot.getDate().equals(appointment.getAppointmentDate()) &&
                    slot.getStartTime() != null && slot.getStartTime().equals(appointment.getStartTime())
                );
        
        if (!isSlotValid) {
            System.out.println("FAILED: Slot mismatch. Requested: " + appointment.getAppointmentDate() + " " + appointment.getStartTime());
            throw new Exception("The selected time slot does not match the doctor's schedule");
        }

        // Overlap Check
        if (hasOverlap(appointment.getDoctorId(), appointment.getPatientId(),
                appointment.getAppointmentDate(), appointment.getStartTime(), appointment.getEndTime())) {
            throw new Exception("This time slot is already booked");
        }

        Appointment saved = appointmentRepository.save(appointment);
        System.out.println("--- Service: SUCCESSFULLY SAVED Appointment! ID: " + saved.getId() + " ---");
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
