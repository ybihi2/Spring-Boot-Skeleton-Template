package com.jydoc.deliverable4.services.impl;

import com.jydoc.deliverable4.dtos.MedicationDTO;
import com.jydoc.deliverable4.dtos.MedicationScheduleDTO;
import com.jydoc.deliverable4.dtos.RefillReminderDTO;
import com.jydoc.deliverable4.model.*;
import com.jydoc.deliverable4.repositories.medicationrepositories.MedicationIntakeTimeRepository;
import com.jydoc.deliverable4.repositories.medicationrepositories.MedicationRepository;
import com.jydoc.deliverable4.repositories.userrepositories.UserRepository;
import com.jydoc.deliverable4.security.Exceptions.MedicationCreationException;
import com.jydoc.deliverable4.security.Exceptions.MedicationNotFoundException;
import com.jydoc.deliverable4.security.Exceptions.MedicationScheduleException;
import com.jydoc.deliverable4.security.Exceptions.UserNotFoundException;
import com.jydoc.deliverable4.services.medicationservices.MedicationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Implementation of the MedicationService interface providing medication management functionality.
 * This service handles CRUD operations for medications, schedule generation, and refill reminders.
 */
@Service
@RequiredArgsConstructor
public class MedicationServiceImpl implements MedicationService {

    private static final Logger logger = LoggerFactory.getLogger(MedicationServiceImpl.class);

    private final MedicationRepository medicationRepository;
    private final UserRepository userRepository;
    private final MedicationIntakeTimeRepository intakeTimeRepository;

    /**
     * Retrieves all medications for a specific user.
     *
     * @param username The username of the user whose medications to retrieve
     * @return List of MedicationDTO objects representing the user's medications
     * @throws UserNotFoundException if the specified user doesn't exist
     */
    @Override
    @Transactional(readOnly = true)
    public List<MedicationDTO> getUserMedications(String username) {
        logger.debug("Fetching medications for user: {}", username);
        long startTime = System.currentTimeMillis();

        try {
            validateUserExists(username);

            List<MedicationModel> medications = medicationRepository.findByUserUsername(username);
            logger.debug("Found {} medications in database query", medications.size());

            List<MedicationDTO> result = medications.stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            logOperationSuccess("retrieved", result.size(), username, startTime);
            return result;
        } catch (Exception e) {
            logOperationError("fetching medications for user", username, e);
            throw e;
        }
    }

    /**
     * Retrieves a single medication by its ID.
     *
     * @param id The ID of the medication to retrieve
     * @return MedicationDTO representing the requested medication
     * @throws MedicationNotFoundException if no medication exists with the specified ID
     */
    @Override
    @Transactional(readOnly = true)
    public MedicationDTO getMedicationById(Long id) {
        logger.debug("Fetching medication by ID: {}", id);

        try {
            MedicationModel medication = medicationRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.error("Medication not found with ID: {}", id);
                        return new MedicationNotFoundException("Medication not found with ID: " + id);
                    });

            MedicationDTO result = convertToDto(medication);
            logger.info("Successfully retrieved medication ID: {}", id);
            return result;
        } catch (Exception e) {
            logOperationError("fetching medication by ID", id.toString(), e);
            throw e;
        }
    }

    /**
     * Creates a new medication for a user.
     *
     * @param medicationDTO DTO containing medication details
     * @param username The username of the user who will own this medication
     * @return MedicationDTO representing the created medication
     * @throws UserNotFoundException if the specified user doesn't exist
     */
    @Override
    @Transactional
    public MedicationDTO createMedication(MedicationDTO medicationDTO, String username) {
        logger.info("Creating new medication for user: {}", username);
        long startTime = System.currentTimeMillis();

        try {
            // Validate and get user
            UserModel user = validateAndGetUser(username);

            // Set default values if not provided
            if (medicationDTO.getActive() == null) {
                medicationDTO.setActive(true);
            }

            // Validate days of week
            if (medicationDTO.getDaysOfWeek() == null || medicationDTO.getDaysOfWeek().isEmpty()) {
                throw new IllegalArgumentException("At least one day of week must be selected");
            }

            // Build and convert the medication
            MedicationModel medication = convertToEntity(medicationDTO, user);

            // Process intake times
            processIntakeTimes(medicationDTO, medication);

            // Process days of week
            processDaysOfWeek(medicationDTO, medication);

            // Save the medication
            MedicationModel savedMedication = saveMedication(medication);

            // Convert to DTO and return
            MedicationDTO result = savedMedication.toDto();
            logOperationSuccess("created", savedMedication.getId(), username, startTime);
            return result;
        } catch (Exception e) {
            logOperationError("creating medication", username, e);
            throw new MedicationCreationException("Failed to create medication: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing medication.
     *
     * @param id The ID of the medication to update
     * @param medicationDTO DTO containing updated medication details
     * @return MedicationDTO representing the updated medication
     * @throws MedicationNotFoundException if no medication exists with the specified ID
     */
    @Override
    @Transactional
    public MedicationDTO updateMedication(Long id, MedicationDTO medicationDTO) {
        logger.info("Updating medication ID: {}", id);
        long startTime = System.currentTimeMillis();

        try {
            MedicationModel existing = getExistingMedication(id);
            updateMedicationFields(existing, medicationDTO);

            if (medicationDTO.getIntakeTimes() != null) {
                logger.debug("Processing {} intake times for update", medicationDTO.getIntakeTimes().size());
                updateIntakeTimes(existing, medicationDTO.getIntakeTimes());
            }

            MedicationModel updatedMedication = medicationRepository.save(existing);
            MedicationDTO result = convertToDto(updatedMedication);

            logOperationSuccess("updated", id, "medication", startTime);
            return result;
        } catch (Exception e) {
            logOperationError("updating medication", id.toString(), e);
            throw new RuntimeException("Failed to update medication", e);
        }
    }

    /**
     * Deletes a medication.
     *
     * @param id The ID of the medication to delete
     * @throws MedicationNotFoundException if no medication exists with the specified ID
     */
    @Override
    @Transactional
    public void deleteMedication(Long id) {
        logger.info("Deleting medication ID: {}", id);
        long startTime = System.currentTimeMillis();

        try {
            validateMedicationExists(id);
            deleteIntakeTimes(id);
            medicationRepository.deleteById(id);

            logOperationSuccess("deleted", id, "medication", startTime);
        } catch (Exception e) {
            logOperationError("deleting medication", id.toString(), e);
            throw new RuntimeException("Failed to delete medication", e);
        }
    }

    /**
     * Generates a medication schedule for a user.
     *
     * @param username The username of the user whose schedule to generate
     * @return List of MedicationScheduleDTO objects representing the schedule
     * @throws MedicationScheduleException if there's an error generating the schedule
     */
    @Override
    @Transactional(readOnly = true)
    public List<MedicationScheduleDTO> getMedicationSchedule(String username) {
        logger.debug("Fetching medication schedule for user: {}", username);
        long startTime = System.currentTimeMillis();

        try {
            List<MedicationModel> medications = medicationRepository.findByUserUsernameWithIntakeTimes(username);
            logger.debug("Found {} medications for schedule", medications.size());

            List<MedicationScheduleDTO> schedule = medications.stream()
                    .filter(Objects::nonNull)
                    .filter(medication -> medication.getIntakeTimes() != null)
                    .flatMap(this::processMedicationForSchedule)
                    .collect(Collectors.toList());

            logger.info("Generated schedule with {} entries for user {} in {} ms",
                    schedule.size(), username, System.currentTimeMillis() - startTime);
            return schedule;
        } catch (Exception e) {
            logger.error("Error generating schedule for user {}: {}", username, e.getMessage(), e);
            throw new MedicationScheduleException("Failed to generate medication schedule", e);
        }
    }

    /**
     * Retrieves upcoming medication refill reminders for a user.
     *
     * @param username The username of the user whose reminders to generate
     * @return List of RefillReminderDTO objects representing the reminders
     */
    @Override
    @Transactional(readOnly = true)
    public List<RefillReminderDTO> getUpcomingRefills(String username) {
        logger.debug("Fetching upcoming refills for user: {}", username);
        long startTime = System.currentTimeMillis();

        try {
            List<MedicationModel> medications = medicationRepository.findByUserUsername(username);
            logger.debug("Found {} medications for refill reminders", medications.size());

            List<RefillReminderDTO> reminders = medications.stream()
                    .map(this::mapToRefillReminderDTO)
                    .collect(Collectors.toList());

            logger.info("Generated {} refill reminders for user {} in {} ms",
                    reminders.size(), username, System.currentTimeMillis() - startTime);
            return reminders;
        } catch (Exception e) {
            logger.error("Error generating refill reminders for user {}: {}", username, e.getMessage(), e);
            throw e;
        }
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Validates that a user exists in the system.
     *
     * @param username The username to validate
     * @throws UserNotFoundException if the user doesn't exist
     */
    private void validateUserExists(String username) {
        if (!userRepository.existsByUsername(username)) {
            logger.error("User not found: {}", username);
            throw new UserNotFoundException(1L);
        }
    }

    /**
     * Validates and retrieves a user entity.
     *
     * @param username The username of the user to retrieve
     * @return UserModel entity
     * @throws UserNotFoundException if the user doesn't exist
     */
    private UserModel validateAndGetUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found: {}", username);
                    return new UserNotFoundException(1L);
                });
    }

    /**
     * Builds a MedicationDTO with user association.
     *
     * @param medicationDTO Source DTO with medication details
     * @param user The user who will own the medication
     * @return Prepared MedicationDTO
     */
    private MedicationDTO buildMedicationDTO(MedicationDTO medicationDTO, UserModel user) {
        return MedicationDTO.builder()
                .medicationName(medicationDTO.getMedicationName())
                .userId(user.getId())
                .urgency(medicationDTO.getUrgency())
                .dosage(medicationDTO.getDosage())
                .instructions(medicationDTO.getInstructions())
                .intakeTimes(medicationDTO.getIntakeTimes() != null ?
                        new HashSet<>(medicationDTO.getIntakeTimes()) : new HashSet<>())
                .daysOfWeek(medicationDTO.getDaysOfWeek() != null ?
                        new HashSet<>(medicationDTO.getDaysOfWeek()) : new HashSet<>())
                .active(medicationDTO.getActive() != null ?
                        medicationDTO.getActive() : true) // Default to true if null
                .build();
    }

    /**
     * Processes intake times for a medication during creation.
     *
     * @param medicationDTO DTO containing intake times
     * @param medication The medication entity to associate with intake times
     */
    private void processIntakeTimes(MedicationDTO medicationDTO, MedicationModel medication) {
        if (medicationDTO.getIntakeTimes() != null && !medicationDTO.getIntakeTimes().isEmpty()) {
            logger.debug("Processing {} intake times", medicationDTO.getIntakeTimes().size());
            medicationDTO.getIntakeTimes().forEach(time -> {
                MedicationIntakeTime mit = MedicationIntakeTime.builder()
                        .medication(medication)
                        .intakeTime(time)
                        .build();
                medication.addIntakeTime(mit.getIntakeTime());
            });
        }
    }

    /**
     * Saves a medication entity and validates the result.
     *
     * @param medication The medication to save
     * @return The saved medication entity
     * @throws RuntimeException if the save operation fails
     */
    private MedicationModel saveMedication(MedicationModel medication) {
        MedicationModel savedMedication = medicationRepository.saveAndFlush(medication);
        if (savedMedication.getId() == null) {
            logger.error("Saved medication has null ID!");
            throw new RuntimeException("Medication ID is null after save");
        }
        return savedMedication;
    }

    /**
     * Retrieves an existing medication entity.
     *
     * @param id The ID of the medication to retrieve
     * @return The medication entity
     * @throws MedicationNotFoundException if the medication doesn't exist
     */
    private MedicationModel getExistingMedication(Long id) {
        return medicationRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Medication not found: {}", id);
                    return new MedicationNotFoundException("Medication not found with ID: " + id);
                });
    }

    /**
     * Validates that a medication exists.
     *
     * @param id The ID of the medication to validate
     * @throws MedicationNotFoundException if the medication doesn't exist
     */
    private void validateMedicationExists(Long id) {
        if (!medicationRepository.existsById(id)) {
            logger.error("Medication not found: {}", id);
            throw new MedicationNotFoundException("Medication not found with ID: " + id);
        }
    }

    /**
     * Deletes intake times associated with a medication.
     *
     * @param medicationId The ID of the medication whose intake times to delete
     */
    private void deleteIntakeTimes(Long medicationId) {
        int deletedIntakeTimes = intakeTimeRepository.deleteByMedicationId(medicationId);
        logger.debug("Deleted {} intake times", deletedIntakeTimes);
    }

    /**
     * Processes a medication for schedule generation.
     *
     * @param medication The medication to process
     * @return Stream of MedicationScheduleDTO objects
     */
    private Stream<MedicationScheduleDTO> processMedicationForSchedule(MedicationModel medication) {
        logger.trace("Processing medication ID: {}", medication.getId());
        return medication.getIntakeTimes().stream()
                .filter(Objects::nonNull)
                .map(intakeTimeEntity -> createScheduleDTO(medication, intakeTimeEntity));
    }

    /**
     * Creates a schedule DTO from medication and intake time.
     *
     * @param medication The medication entity
     * @param intakeTimeEntity The intake time entity
     * @return MedicationScheduleDTO
     */
    private MedicationScheduleDTO createScheduleDTO(MedicationModel medication, MedicationIntakeTime intakeTimeEntity) {
        if (intakeTimeEntity == null) {
            throw new IllegalArgumentException("Intake time entity cannot be null");
        }
        return createScheduleDTO(medication, intakeTimeEntity.getIntakeTime());
    }

    /**
     * Creates a schedule DTO from medication and time.
     *
     * @param medication The medication entity
     * @param intakeTime The intake time
     * @return MedicationScheduleDTO
     */
    private MedicationScheduleDTO createScheduleDTO(MedicationModel medication, LocalTime intakeTime) {
        if (intakeTime == null) {
            logger.warn("Null intake time encountered for medication ID: {}", medication.getId());
            intakeTime = LocalTime.MIDNIGHT;
        }

        return MedicationScheduleDTO.builder()
                .medicationId(medication.getId())
                .medicationName(medication.getName())
                .dosage(medication.getDosage())
                .scheduleTime(intakeTime)
                .isTaken(false)
                .instructions(medication.getInstructions())
                .status("UPCOMING")
                .urgency(convertToDtoUrgency(medication.getUrgency()))
                .build();
    }

    /**
     * Converts medication urgency to DTO format.
     *
     * @param modelUrgency The urgency from the model
     * @return Converted urgency for DTO
     */
    private MedicationScheduleDTO.MedicationUrgency convertToDtoUrgency(MedicationModel.MedicationUrgency modelUrgency) {
        if (modelUrgency == null) {
            logger.debug("Null urgency encountered, defaulting to ROUTINE");
            return MedicationScheduleDTO.MedicationUrgency.ROUTINE;
        }
        try {
            return MedicationScheduleDTO.MedicationUrgency.valueOf(modelUrgency.name());
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown urgency value: {}, defaulting to ROUTINE", modelUrgency);
            return MedicationScheduleDTO.MedicationUrgency.ROUTINE;
        }
    }

    /**
     * Updates intake times for a medication.
     *
     * @param medication The medication to update
     * @param newIntakeTimes The new intake times to set
     */
    private void updateIntakeTimes(MedicationModel medication, Set<LocalTime> newIntakeTimes) {
        Set<LocalTime> currentTimes = medication.getIntakeTimesAsLocalTimes();

        // Remove times that are no longer present
        currentTimes.stream()
                .filter(time -> !newIntakeTimes.contains(time))
                .forEach(medication::removeIntakeTime);

        // Add new times that aren't already present
        newIntakeTimes.stream()
                .filter(time -> !currentTimes.contains(time))
                .forEach(medication::addIntakeTime);

        logger.trace("Intake times updated for medication ID: {}. Total times now: {}",
                medication.getId(), medication.getIntakeTimes().size());
    }

    /**
     * Updates medication fields from DTO.
     *
     * @param medication The medication to update
     * @param dto The DTO containing new values
     */
    private void updateMedicationFields(MedicationModel medication, MedicationDTO dto) {
        logger.trace("Updating fields for medication ID: {}", medication.getId());

        if (dto.getMedicationName() != null) {
            medication.setName(dto.getMedicationName());
        } else {
            logger.warn("Medication name is null in DTO for medication ID: {}", medication.getId());
        }

        if (dto.getUrgency() != null) {
            try {
                medication.setUrgency(MedicationModel.MedicationUrgency.valueOf(dto.getUrgency().name()));
            } catch (IllegalArgumentException e) {
                logger.error("Invalid urgency value in DTO: {}. Keeping existing value for medication ID: {}",
                        dto.getUrgency(), medication.getId());
            }
        } else {
            logger.warn("Urgency is null in DTO for medication ID: {}", medication.getId());
        }

        medication.setDosage(dto.getDosage());
        medication.setInstructions(dto.getInstructions());
    }

    /**
     * Converts a medication entity to DTO.
     *
     * @param medication The medication to convert
     * @return MedicationDTO
     */
    private MedicationDTO convertToDto(MedicationModel medication) {
        if (medication == null) {
            logger.warn("Attempted to convert null MedicationModel to DTO");
            return null;
        }

        logger.trace("Converting medication ID {} to DTO", medication.getId());
        Set<LocalTime> intakeTimes = medication.getIntakeTimesAsLocalTimes();

        try {
            return MedicationDTO.builder()
                    .id(medication.getId())
                    .userId(medication.getUser() != null ? medication.getUser().getId() : null)
                    .medicationName(medication.getName())
                    .urgency(medication.getUrgency() != null ?
                            MedicationDTO.MedicationUrgency.valueOf(medication.getUrgency().name()) : null)
                    .dosage(medication.getDosage())
                    .instructions(medication.getInstructions())
                    .intakeTimes(intakeTimes)
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("Failed to convert urgency {} to DTO enum for medication ID: {}",
                    medication.getUrgency(), medication.getId(), e);
            throw new RuntimeException("Invalid urgency value during DTO conversion", e);
        }
    }

    /**
     * Converts a medication DTO to entity.
     *
     * @param dto The DTO to convert
     * @param user The user who will own the medication
     * @return MedicationModel
     */
    private MedicationModel convertToEntity(MedicationDTO dto, UserModel user) {
        logger.trace("Converting DTO to medication entity for user ID: {}", user.getId());

        return MedicationModel.builder()
                .user(user)
                .name(dto.getMedicationName())
                .urgency(MedicationModel.MedicationUrgency.valueOf(dto.getUrgency().name()))
                .dosage(dto.getDosage())
                .instructions(dto.getInstructions())
                .build();
    }

    /**
     * Maps a medication to a refill reminder DTO.
     *
     * @param medication The medication to map
     * @return RefillReminderDTO
     */
    private RefillReminderDTO mapToRefillReminderDTO(MedicationModel medication) {
        logger.trace("Mapping medication ID {} to refill reminder", medication.getId());

        int remainingDoses = calculateRemainingDoses(medication);
        logger.trace("Calculated {} remaining doses", remainingDoses);

        return RefillReminderDTO.builder()
                .medicationId(medication.getId())
                .medicationName(medication.getName())
                .remainingDoses(remainingDoses)
                .refillByDate(LocalDate.now().plusDays(7))
                .urgency(medication.getUrgency().name())
                .build();
    }

    /**
     * Calculates remaining doses for a medication.
     *
     * @param medication The medication to calculate for
     * @return Estimated number of remaining doses
     */
    private int calculateRemainingDoses(MedicationModel medication) {
        int count = medication.getIntakeTimes().size() * 7; // Assume 1 week supply
        logger.trace("Calculated remaining doses: {} ({} intake times × 7 days)",
                count, medication.getIntakeTimes().size());
        return count;
    }

    /**
     * Logs a successful operation.
     *
     * @param operation Description of the operation performed
     * @param identifier Identifier of the affected entity
     * @param entityType Type of entity affected
     * @param startTime Start time of the operation (for duration calculation)
     */
    private void logOperationSuccess(String operation, Object identifier, String entityType, long startTime) {
        logger.info("Successfully {} {} {} in {} ms",
                operation, entityType, identifier, System.currentTimeMillis() - startTime);
    }

    /**
     * Logs an operation error.
     *
     * @param operation Description of the operation attempted
     * @param identifier Identifier of the affected entity
     * @param exception The exception that occurred
     */
    private void logOperationError(String operation, String identifier, Exception exception) {
        logger.error("Error {} {}: {}", operation, identifier, exception.getMessage(), exception);
    }

    private void processDaysOfWeek(MedicationDTO medicationDTO, MedicationModel medication) {
        medicationDTO.getDaysOfWeek().forEach(day ->
                medication.addDay(MedicationModel.DayOfWeek.valueOf(day.name())));
    }

}