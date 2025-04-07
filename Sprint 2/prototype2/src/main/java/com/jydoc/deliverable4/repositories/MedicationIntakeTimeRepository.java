package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.MedicationIntakeTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface MedicationIntakeTimeRepository extends JpaRepository<MedicationIntakeTime, Long> {

    // Find all intake times for a specific medication
    List<MedicationIntakeTime> findByMedicationId(Long medicationId);

    // Delete all intake times for a specific medication
    @Modifying
    @Query("DELETE FROM MedicationIntakeTime mit WHERE mit.medication.id = :medicationId")
    int deleteByMedicationId(@Param("medicationId") Long medicationId);

    @Modifying
    @Query("DELETE FROM MedicationIntakeTime mit WHERE mit.medication.id = :medicationId AND mit.intakeTime = :intakeTime")
    void deleteByMedicationIdAndIntakeTime(@Param("medicationId") Long medicationId, @Param("intakeTime") LocalTime intakeTime);

    // Check if a specific intake time exists for a medication
    boolean existsByMedicationIdAndIntakeTime(Long medicationId, LocalTime intakeTime);

    // Find intake times by time range for a specific medication
    @Query("SELECT mit FROM MedicationIntakeTime mit WHERE mit.medication.id = :medicationId " +
            "AND mit.intakeTime BETWEEN :startTime AND :endTime")
    List<MedicationIntakeTime> findByMedicationAndTimeRange(
            @Param("medicationId") Long medicationId,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);



}