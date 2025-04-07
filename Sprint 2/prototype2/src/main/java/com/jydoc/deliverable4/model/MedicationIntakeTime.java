package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a scheduled medication intake time associated with a specific medication.
 * This entity maps to the 'medication_intake_times' table in the database.
 */
@Entity
@Table(name = "medication_intake_times")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicationIntakeTime {
    private static final Logger logger = LoggerFactory.getLogger(MedicationIntakeTime.class);

    /**
     * Unique identifier for the medication intake time record.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The medication associated with this intake time.
     * This is a many-to-one relationship with MedicationModel.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_id", nullable = false)
    private MedicationModel medication;

    /**
     * The scheduled time for medication intake.
     * This field cannot be null.
     */
    @Column(nullable = false)
    private LocalTime intakeTime;

    /**
     * Constructs a new MedicationIntakeTime with the specified medication and intake time.
     *
     * @param medication The medication associated with this intake time (cannot be null)
     * @param intakeTime The scheduled time for medication intake (cannot be null)
     * @throws IllegalArgumentException if intakeTime is null
     * @throws NullPointerException if medication is null
     */
    public MedicationIntakeTime(MedicationModel medication, LocalTime intakeTime) {
        logger.debug("Constructing new MedicationIntakeTime with medication: {} and intake time: {}",
                medication, intakeTime);

        if (intakeTime == null) {
            logger.error("Attempted to create MedicationIntakeTime with null intakeTime");
            throw new IllegalArgumentException("Intake time cannot be null");
        }

        this.medication = Objects.requireNonNull(medication, "Medication cannot be null");
        this.intakeTime = Objects.requireNonNull(intakeTime, "Intake time cannot be null");

        logger.info("Created new MedicationIntakeTime for medication ID: {} at time: {}",
                medication.getId(), intakeTime);
    }

    /**
     * Sets the medication associated with this intake time.
     * Maintains bidirectional relationship by updating both sides of the association.
     *
     * @param medication The medication to associate with this intake time (cannot be null)
     * @throws NullPointerException if medication is null
     */
    public void setMedication(MedicationModel medication) {
        logger.debug("Setting medication for intake time ID: {}. New medication ID: {}",
                this.id, medication != null ? medication.getId() : "null");

        Objects.requireNonNull(medication, "Medication cannot be null");

        // Remove this intake time from the previous medication's list
        if (this.medication != null) {
            logger.trace("Removing intake time from previous medication's list");
            this.medication.getIntakeTimes().remove(this);
        }

        this.medication = medication;

        // Add this intake time to the new medication's list if not already present
        if (!medication.getIntakeTimes().contains(this)) {
            logger.trace("Adding intake time to new medication's list");
            medication.getIntakeTimes().add(this);
        }

        logger.info("Updated medication association for intake time ID: {}. New medication ID: {}",
                this.id, medication.getId());
    }

    /**
     * Sets the scheduled time for medication intake.
     *
     * @param intakeTime The time for medication intake (cannot be null)
     * @throws NullPointerException if intakeTime is null
     */
    public void setIntakeTime(LocalTime intakeTime) {
        logger.debug("Setting intake time for ID: {}. New time: {}", this.id, intakeTime);

        if (intakeTime == null) {
            logger.error("Attempted to set null intake time for ID: {}", this.id);
            throw new NullPointerException("Intake time cannot be null");
        }

        this.intakeTime = Objects.requireNonNull(intakeTime, "Intake time cannot be null");
        logger.info("Updated intake time for ID: {}. New time: {}", this.id, intakeTime);
    }

    /**
     * Compares this MedicationIntakeTime with another object for equality.
     * Two MedicationIntakeTimes are considered equal if they have the same
     * medication and intake time.
     *
     * @param o The object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            logger.trace("Equals comparison with same instance");
            return true;
        }

        if (!(o instanceof MedicationIntakeTime)) {
            logger.trace("Equals comparison with different type");
            return false;
        }

        MedicationIntakeTime that = (MedicationIntakeTime) o;
        boolean isEqual = getIntakeTime().equals(that.getIntakeTime()) &&
                getMedication().equals(that.getMedication());

        logger.debug("Equals comparison result: {} for IDs: {} and {}",
                isEqual, this.id, that.id);

        return isEqual;
    }

    /**
     * Generates a hash code for this MedicationIntakeTime based on
     * its medication and intake time.
     *
     * @return The computed hash code
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(getIntakeTime(), getMedication());
        logger.trace("Generated hash code: {} for ID: {}", hash, this.id);
        return hash;
    }

    /**
     * Returns a string representation of this MedicationIntakeTime.
     *
     * @return String representation of the object
     */
    @Override
    public String toString() {
        return "MedicationIntakeTime{" +
                "id=" + id +
                ", medicationId=" + (medication != null ? medication.getId() : "null") +
                ", intakeTime=" + intakeTime +
                '}';
    }
}