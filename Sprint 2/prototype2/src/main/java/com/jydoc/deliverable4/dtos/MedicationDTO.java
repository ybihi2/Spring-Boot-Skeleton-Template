package com.jydoc.deliverable4.dtos;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Data Transfer Object (DTO) representing medication information in the system.
 * This class serves as the primary data structure for transferring medication-related data
 * between different layers of the application while enforcing validation rules.
 *
 * <p>The class includes comprehensive validation annotations to ensure data integrity
 * and provides utility methods for common medication-related operations.</p>
 *
 * <p><b>Usage Example:</b>
 * <pre>{@code
 * MedicationDTO medication = MedicationDTO.builder()
 *     .medicationName("Ibuprofen")
 *     .urgency(MedicationUrgency.ROUTINE)
 *     .intakeTimes(Set.of(LocalTime.of(8, 0), LocalTime.of(20, 0)))
 *     .daysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
 *     .dosage("200mg")
 *     .build();
 * }</pre>
 * </p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicationDTO {

    /**
     * Unique identifier for the medication record.
     * This field is automatically generated by the persistence layer.
     */
    private Long id;

    /**
     * Identifier of the user associated with this medication.
     * Must not be null.
     */
    @NotNull(message = "User ID cannot be null")
    private Long userId;

    /**
     * Name of the medication. Must not be blank.
     * <p>Example: "Amoxicillin", "Lisinopril"</p>
     */
    @NotBlank(message = "Medication name cannot be blank")
    private String medicationName;

    /**
     * Urgency level of the medication. Must not be null.
     * See {@link MedicationUrgency} for possible values.
     */
    @NotNull(message = "Urgency level must be specified")
    private MedicationUrgency urgency;

    /**
     * Set of times when the medication should be taken.
     * Must not be null, but can be empty for PRN medications.
     */
    @Builder.Default
    @NotNull(message = "Intake times must be specified")
    private List<LocalTime> intakeTimes = new ArrayList<>();

    /**
     * Days of the week when the medication should be taken.
     * Must not be null, but can be empty for as-needed medications.
     * See {@link DayOfWeek} for possible values.
     */
    @NotNull(message = "Days of week must be specified")
    private Set<DayOfWeek> daysOfWeek;

    /**
     * Dosage information for the medication.
     * <p>Example: "200mg", "1 tablet"</p>
     */
    private String dosage;

    /**
     * Special instructions for taking the medication.
     * <p>Example: "Take with food", "Avoid alcohol"</p>
     */
    private String instructions;

    /**
     * Flag indicating whether the medication is currently active.
     * Null values are treated as inactive.
     */
    private Boolean active;

    /**
     * Enumeration representing the urgency level of a medication.
     *
     * <p><b>Possible Values:</b>
     * <ul>
     *   <li>URGENT - Requires immediate attention</li>
     *   <li>NONURGENT - Important but not time-critical</li>
     *   <li>ROUTINE - Standard scheduled medication</li>
     * </ul>
     */
    public enum MedicationUrgency {
        URGENT,
        NONURGENT,
        ROUTINE;

        /**
         * Converts a string value to the corresponding MedicationUrgency enum.
         *
         * @param value The string value to convert (case-insensitive)
         * @return Corresponding MedicationUrgency enum
         * @throws IllegalArgumentException if the value doesn't match any enum constant
         * @apiNote Returns ROUTINE as default if input is null
         */
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

    /**
     * Enumeration representing days of the week with display names.
     * Each enum constant includes a user-friendly display name.
     */
    @Getter
    public enum DayOfWeek {
        MONDAY("Monday"),
        TUESDAY("Tuesday"),
        WEDNESDAY("Wednesday"),
        THURSDAY("Thursday"),
        FRIDAY("Friday"),
        SATURDAY("Saturday"),
        SUNDAY("Sunday");

        private final String displayName;

        /**
         * Constructs a DayOfWeek enum constant with the specified display name.
         *
         * @param displayName The user-friendly name of the day
         */
        DayOfWeek(String displayName) {
            this.displayName = displayName;
        }

        /**
         * Converts a specific date (day, year, month) to the corresponding DayOfWeek.
         *
         * @param day The day of the month (1-31)
         * @param year The year (e.g., 2025)
         * @param month The month (1-12)
         * @return The DayOfWeek enum value corresponding to the given date
         * @throws IllegalArgumentException if the date is invalid
         */
        public static DayOfWeek fromDayOfMonth(Integer day, Integer year, Integer month) {
            try {
                LocalDate date = LocalDate.of(year, month, day);
                java.time.DayOfWeek javaDayOfWeek = date.getDayOfWeek();
                switch (javaDayOfWeek) {
                    case MONDAY: return MONDAY;
                    case TUESDAY: return TUESDAY;
                    case WEDNESDAY: return WEDNESDAY;
                    case THURSDAY: return THURSDAY;
                    case FRIDAY: return FRIDAY;
                    case SATURDAY: return SATURDAY;
                    case SUNDAY: return SUNDAY;
                    default: throw new IllegalArgumentException("Unknown day of week: " + javaDayOfWeek);
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date parameters: day=" + day + ", year=" + year + ", month=" + month, e);
            }
        }
    }

    /**
     * Determines if the medication is currently active.
     *
     * @return true if the medication is explicitly marked as active,
     *         false otherwise (including null active status)
     */
    public boolean isActive() {
        return active != null && active;
    }
}