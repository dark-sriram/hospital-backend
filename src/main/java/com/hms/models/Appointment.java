package com.hms.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "appointments")
public class Appointment {
    @Id
    private String id;
    private String patientId;
    private String doctorId;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private Status status;
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        BOOKED, CONFIRMED, COMPLETED, CANCELLED
    }
}
