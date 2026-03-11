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
        // 1. Doctor Availability
        User doctor = userRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new Exception("Doctor not found"));
        boolean available = doctor.getAvailableSlots().stream()
                .anyMatch(slot -> slot.getDate().equals(appointment.getAppointmentDate()) &&
                        slot.getStartTime().compareTo(appointment.getStartTime()) <= 0 &&
                        slot.getEndTime().compareTo(appointment.getEndTime()) >= 0);
        if (!available)
            throw new Exception("Doctor not available at this time");

        // 2. Overlaps
        if (hasOverlap(appointment.getDoctorId(), appointment.getPatientId(),
                appointment.getAppointmentDate(), appointment.getStartTime(), appointment.getEndTime())) {
            throw new Exception("Time slot overlap detected");
        }

        appointment.setStatus(Appointment.Status.BOOKED);
        return appointmentRepository.save(appointment);
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
