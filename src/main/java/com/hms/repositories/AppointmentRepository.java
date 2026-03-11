package com.hms.repositories;

import com.hms.models.Appointment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface AppointmentRepository extends MongoRepository<Appointment, String> {
    List<Appointment> findByPatientId(String patientId);
    List<Appointment> findByDoctorId(String doctorId);
    List<Appointment> findByDoctorIdAndStatusNot(String doctorId, Appointment.Status status);
    List<Appointment> findByPatientIdAndStatusNot(String patientId, Appointment.Status status);
    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(String doctorId, String date, Appointment.Status status);
    List<Appointment> findByPatientIdAndAppointmentDateAndStatusNot(String patientId, String date, Appointment.Status status);
}
