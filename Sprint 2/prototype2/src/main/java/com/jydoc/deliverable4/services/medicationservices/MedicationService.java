package com.jydoc.deliverable4.services.medicationservices;

import com.jydoc.deliverable4.dtos.MedicationDTO;
import com.jydoc.deliverable4.dtos.MedicationScheduleDTO;
import com.jydoc.deliverable4.dtos.RefillReminderDTO;
import com.jydoc.deliverable4.security.Exceptions.MedicationNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * Service interface for medication-related operations.
 * <p>
 * Defines the contract for services that manage medication data including CRUD operations,
 * scheduling, and refill reminders. This interface serves as the main API for all
 * medication management functionality in the system.
 * </p>
 */
@Service
public interface MedicationService {

    MedicationDTO getMedicationById(Long id, String username); // New method



    /**
     * Retrieves all medications associated with a specific user.
     *
     * @param username The username of the patient whose medications to retrieve. Must not be null or empty.
     * @return A list of {@link MedicationDTO} objects representing the user's medications.
     *         Returns empty list if no medications found.
     * @throws IllegalArgumentException if username is null or empty
     */
    List<MedicationDTO> getUserMedications(String username);

    /**
     * Retrieves a specific medication by its unique identifier.
     *
     * @param id The unique identifier of the medication to retrieve. Must not be null.
     * @return The {@link MedicationDTO} representing the requested medication
     * @throws IllegalArgumentException if id is null
     * @throws com.jydoc.deliverable4.security.Exceptions.MedicationNotFoundException if medication with specified id doesn't exist
     */
    MedicationDTO getMedicationById(Long id);

    /**
     * Creates a new medication record for the specified user.
     *
     * @param medicationDTO The medication data to create. Must not be null and must contain valid data.
     * @param username The username of the patient who will own this medication. Must not be null or empty.
     * @return The created {@link MedicationDTO} with generated fields populated (e.g., id)
     * @throws IllegalArgumentException if either parameter is null or contains invalid data
     */
    MedicationDTO createMedication(MedicationDTO medicationDTO, String username);

    /**
     * Updates an existing medication record.
     *
     * @param id The unique identifier of the medication to update. Must not be null.
     * @param medicationDTO The updated medication data. Must not be null and must contain valid data.
     * @return The updated {@link MedicationDTO}
     * @throws IllegalArgumentException if either parameter is null or contains invalid data
     * @throws com.jydoc.deliverable4.security.Exceptions.MedicationNotFoundException if medication with specified id doesn't exist
     */
    MedicationDTO updateMedication(Long id, MedicationDTO medicationDTO);

    /**
     * Deletes a medication record.
     *
     * @param id The unique identifier of the medication to delete. Must not be null.
     * @throws IllegalArgumentException if id is null
     * @throws com.jydoc.deliverable4.security.Exceptions.MedicationNotFoundException if medication with specified id doesn't exist
     */
    void deleteMedication(Long id);

    /**
     * Retrieves the days of medication intake for a specific medication.
     *
     * @param medicationId The ID of the medication to retrieve intake days for
     * @return Set of days of the week when the medication should be taken
     * @throws MedicationNotFoundException if no medication exists with the specified ID
     */
    Set<MedicationDTO.DayOfWeek> getMedicationIntakeDays(Long medicationId);

    /**
     * Retrieves the medication schedule for a specific user.
     *
     * @param username The username of the patient whose schedule to retrieve. Must not be null or empty.
     * @return A list of {@link MedicationScheduleDTO} objects representing the user's medication schedule.
     *         Returns empty list if no scheduled medications found.
     * @throws IllegalArgumentException if username is null or empty
     */
    List<MedicationScheduleDTO> getMedicationSchedule(String username);

    /**
     * Retrieves upcoming medication refill reminders for a specific user.
     *
     * @param username The username of the patient whose refill reminders to retrieve. Must not be null or empty.
     * @return A list of {@link RefillReminderDTO} objects representing upcoming refills.
     *         Returns empty list if no upcoming refills found.
     * @throws IllegalArgumentException if username is null or empty
     */
    List<RefillReminderDTO> getUpcomingRefills(String username);
}