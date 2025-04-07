package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.MedicationModel;
import com.jydoc.deliverable4.model.MedicationModel.MedicationUrgency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MedicationRepository extends JpaRepository<MedicationModel, Long> {

    // Basic user medication query
    List<MedicationModel> findByUserUsername(String username);

    // In MedicationRepository.java
    @Query("SELECT DISTINCT m FROM MedicationModel m LEFT JOIN FETCH m.intakeTimes WHERE m.user.username = :username")
    List<MedicationModel> findByUserUsernameWithIntakeTimes(@Param("username") String username);




}