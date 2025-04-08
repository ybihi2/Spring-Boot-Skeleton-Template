package com.jydoc.deliverable4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing an upcoming medication for the dashboard view.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingMedicationDto {
    private String name;
    private String dosage;
    private String nextDoseTime;
    private boolean taken;

    // Optional additional fields that might be useful
    private String instructions;
    private String status; // "UPCOMING", "MISSED", "TAKEN"
    private String urgency; // "URGENT", "NONURGENT", "ROUTINE"

    /**
     * Helper method to get a CSS class for status display
     */
    public String getStatusBadgeClass() {
        if (taken) {
            return "badge bg-success";
        }
        return "badge bg-warning text-dark";
    }

    /**
     * Helper method to get status text
     */
    public String getStatusText() {
        return taken ? "Taken" : "Pending";
    }
}