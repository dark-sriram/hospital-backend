package com.hms.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Data
@Document(collection = "users")
public class User {
    @Id
    @JsonProperty("_id")
    private String id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private String specialization;
    private List<Slot> availableSlots;

    public enum Role {
        ADMIN, DOCTOR, PATIENT
    }

    @Data
    public static class Slot {
        private String date;
        private String startTime;
        private String endTime;
    }
}
