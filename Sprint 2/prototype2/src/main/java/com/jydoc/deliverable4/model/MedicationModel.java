package com.jydoc.deliverable4.model;

import com.jydoc.deliverable4.dtos.MedicationDTO;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity class representing a medication in the system.
 * <p>
 * This class models a medication prescribed to a user, including its properties,
 * intake times, urgency level, and other relevant information.
 * </p>
 */
@Entity
@Table(name = "medications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class MedicationModel {
    private static final Logger logger = LoggerFactory.getLogger(MedicationModel.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    private UserModel user;

    @Column(nullable = false)
    @NotBlank
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private MedicationUrgency urgency;

    @OneToMany(mappedBy = "medication", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<MedicationIntakeTime> intakeTimes = new HashSet<>();

    private String dosage;
    private String instructions;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Adds an intake time for this medication.
     * <p>
     * Ensures bidirectional relationship consistency and prevents duplicate times.
     * </p>
     *
     * @param time The LocalTime to add as an intake time (must not be null)
     * @throws NullPointerException if the time parameter is null
     */
    public void addIntakeTime(LocalTime time) {
        logger.debug("Attempting to add intake time: {} for medication ID: {}", time, id);
        Objects.requireNonNull(time, "Intake time cannot be null");

        if (getIntakeTimesAsLocalTimes().contains(time)) {
            logger.info("Intake time {} already exists for medication ID: {}, skipping addition", time, id);
            return;
        }

        MedicationIntakeTime newIntake = new MedicationIntakeTime(this, time);
        intakeTimes.add(newIntake);
        logger.info("Added new intake time: {} for medication ID: {}", time, id);
    }

    /**
     * Removes an intake time from this medication.
     * <p>
     * Handles the bidirectional relationship cleanup.
     * </p>
     *
     * @param time The LocalTime to remove (must not be null)
     * @throws NullPointerException if the time parameter is null
     */
    public void removeIntakeTime(LocalTime time) {
        logger.debug("Attempting to remove intake time: {} from medication ID: {}", time, id);
        Objects.requireNonNull(time, "Intake time cannot be null");

        int initialSize = intakeTimes.size();
        intakeTimes.removeIf(intake -> time.equals(intake.getIntakeTime()));

        if (intakeTimes.size() < initialSize) {
            logger.info("Removed intake time: {} from medication ID: {}", time, id);
        } else {
            logger.debug("No matching intake time found for removal: {} in medication ID: {}", time, id);
        }
    }

    /**
     * Sets the intake times for this medication.
     * <p>
     * Ensures bidirectional relationship consistency and prevents null values.
     * This is a private method as direct replacement of the set should be controlled.
     * </p>
     *
     * @param intakeTimes The set of MedicationIntakeTime objects to set
     */
    private void setIntakeTimes(Set<MedicationIntakeTime> intakeTimes) {
        logger.debug("Setting intake times for medication ID: {}", id);
        this.intakeTimes = intakeTimes != null ? intakeTimes : new HashSet<>();

        // Ensure bidirectional consistency
        this.intakeTimes.forEach(it -> {
            it.setMedication(this);
            logger.trace("Ensured bidirectional consistency for intake time: {} in medication ID: {}",
                    it.getIntakeTime(), id);
        });
    }

    /**
     * Gets all intake times as LocalTime objects.
     *
     * @return A Set of LocalTime representing all intake times for this medication
     */
    public Set<LocalTime> getIntakeTimesAsLocalTimes() {
        logger.debug("Retrieving intake times as LocalTime for medication ID: {}", id);
        return intakeTimes.stream()
                .map(MedicationIntakeTime::getIntakeTime)
                .collect(Collectors.toSet());
    }

    /**
     * Enum representing the urgency level of a medication.
     */
    public enum MedicationUrgency {
        /** Medication requires immediate attention */
        URGENT,
        /** Medication is important but not immediately critical */
        NONURGENT,
        /** Regular medication without special urgency */
        ROUTINE
    }

    /**
     * Converts this MedicationModel to a MedicationDTO.
     * <p>
     * Used for data transfer between layers while avoiding entity exposure.
     * </p>
     *
     * @return A MedicationDTO representation of this model
     */
    public MedicationDTO toDto() {
        logger.debug("Converting MedicationModel to DTO for medication ID: {}", id);
        return MedicationDTO.builder()
                .id(this.id)
                .userId(this.user.getId())
                .medicationName(this.name)
                .urgency(this.urgency != null ?
                        MedicationDTO.MedicationUrgency.valueOf(this.urgency.name()) : null)
                .intakeTimes(this.getIntakeTimesAsLocalTimes())
                .dosage(this.dosage)
                .instructions(this.instructions)
                .build();
    }
}