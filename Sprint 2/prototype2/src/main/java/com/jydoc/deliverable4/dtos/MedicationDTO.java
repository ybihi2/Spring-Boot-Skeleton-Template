package com.jydoc.deliverable4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;

/**
 * Data Transfer Object for medication information.
 * Includes validation annotations and builder customization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {

    private Long id;  // Added ID field for medication identification

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotBlank(message = "Medication name cannot be blank")
    private String medicationName;

    @NotNull(message = "Urgency level must be specified")
    private MedicationUrgency urgency;

    @NotNull(message = "Intake times must be specified")
    private Set<LocalTime> intakeTimes;

    @NotNull(message = "Days of week must be specified")
    private Set<DayOfWeek> daysOfWeek;

    // Additional recommended fields
    private String dosage;
    private String instructions;
    private Boolean active;

    public enum MedicationUrgency {
        URGENT,
        NONURGENT,
        ROUTINE;

        public static MedicationUrgency fromString(String value) {
            if (value == null) {
                return ROUTINE;  // Default value
            }
            try {
                return MedicationUrgency.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid urgency value. Must be 'URGENT', 'NONURGENT' or 'ROUTINE'");
            }
        }
    }

    public enum DayOfWeek {
        MONDAY,
        TUESDAY,
        WEDNESDAY,
        THURSDAY,
        FRIDAY,
        SATURDAY,
        SUNDAY;

        public static DayOfWeek fromString(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Day of week cannot be null");
            }
            try {
                return DayOfWeek.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid day of week. Must be one of: " +
                                "MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY");
            }
        }
    }



    /**
     * Helper method to check if medication is active
     */
    public boolean isActive() {
        return active != null && active;
    }
}