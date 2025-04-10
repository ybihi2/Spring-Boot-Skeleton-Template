package com.jydoc.deliverable4.repositories.medicationrepositories;

import com.jydoc.deliverable4.model.MedicationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MedicationRepository extends JpaRepository<MedicationModel, Long> {


    Optional<MedicationModel> findByIdAndUserUsername(Long id, String username);


    // Basic user medication query
    List<MedicationModel> findByUserUsername(String username);

    // In MedicationRepository.java
    @Query("SELECT DISTINCT m FROM MedicationModel m LEFT JOIN FETCH m.intakeTimes WHERE m.user.username = :username")
    List<MedicationModel> findByUserUsernameWithIntakeTimes(@Param("username") String username);


    @Query("SELECT DISTINCT m FROM MedicationModel m " +
            "LEFT JOIN FETCH m.intakeTimes " +
            "LEFT JOIN FETCH m.daysOfWeek " +
            "WHERE m.user.username = :username")
    List<MedicationModel> findByUserUsernameWithMedicationDetails(@Param("username") String username);

}