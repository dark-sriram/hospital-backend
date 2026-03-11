package com.hms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hms.models.Appointment;
import lombok.Data;

@Data
public class AppointmentResponse {
    @JsonProperty("_id")
    private String id;
    private UserShort patientId;
    private UserShort doctorId;
    private String appointmentDate;
    private String startTime;
    private String endTime;
    private Appointment.Status status;

    @Data
    public static class UserShort {
        @JsonProperty("_id")
        private String id;
        private String name;
    }
}
