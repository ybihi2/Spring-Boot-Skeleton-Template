package com.jydoc.deliverable4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Set;

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

    // Additional recommended fields
    private String dosage;
    private String instructions;
    private Boolean active;

    public enum MedicationUrgency {
        URGENT,
        NONURGENT,
        ROUTINE;  // Added more options

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

    // Builder customization to handle null values
    public static class MedicationDTOBuilder {
        private Set<LocalTime> intakeTimes = new java.util.HashSet<>();

        public MedicationDTOBuilder intakeTimes(Set<LocalTime> intakeTimes) {
            if (intakeTimes != null) {
                this.intakeTimes.addAll(intakeTimes);
            }
            return this;
        }
    }
}