package com.jydoc.deliverable4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationScheduleDTO {

    private MedicationUrgency urgency;  // Using enum type

    public enum MedicationUrgency {
        URGENT, NONURGENT, ROUTINE
    }


    private Long medicationId;
    private String medicationName;
    private String dosage;
    private LocalTime scheduleTime;
    private boolean isTaken;
    private String instructions;
    private String status; // "UPCOMING", "MISSED", "TAKEN", etc.

    public String getFormattedTime() {
        return scheduleTime != null ? scheduleTime.toString() : "";
    }

    public String getStatusBadgeClass() {
        return switch (status) {
            case "TAKEN" -> "badge bg-success";
            case "MISSED" -> "badge bg-danger";
            default -> "badge bg-warning text-dark";
        };
    }
}