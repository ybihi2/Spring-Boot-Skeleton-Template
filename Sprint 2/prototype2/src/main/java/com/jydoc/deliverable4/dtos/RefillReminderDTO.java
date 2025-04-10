package com.jydoc.deliverable4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefillReminderDTO {
    private Long medicationId;
    private String medicationName;
    private int remainingDoses;
    private LocalDate refillByDate;
    private String pharmacyInfo;
    private String urgency; // "CRITICAL", "WARNING", "INFO"

    public String getUrgencyBadgeClass() {
        return switch (urgency) {
            case "CRITICAL" -> "badge bg-danger";
            case "WARNING" -> "badge bg-warning text-dark";
            default -> "badge bg-info text-dark";
        };
    }

    public String getDaysUntilRefill() {
        if (refillByDate == null) return "N/A";
        long days = LocalDate.now().datesUntil(refillByDate).count();
        return days + " day" + (days != 1 ? "s" : "");
    }
}