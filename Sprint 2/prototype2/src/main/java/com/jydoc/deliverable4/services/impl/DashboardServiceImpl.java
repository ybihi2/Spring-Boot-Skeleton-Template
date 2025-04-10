package com.jydoc.deliverable4.services.impl;

import com.jydoc.deliverable4.dtos.MedicationScheduleDTO;
import com.jydoc.deliverable4.dtos.userdtos.DashboardDTO;
import com.jydoc.deliverable4.model.MedicationModel;
import com.jydoc.deliverable4.repositories.medicationrepositories.MedicationRepository;
import com.jydoc.deliverable4.services.userservices.DashboardService;
import com.jydoc.deliverable4.services.medicationservices.MedicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the DashboardService interface that provides methods for retrieving
 * and processing user dashboard data including medication schedules, health metrics,
 * and alerts.
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final MedicationService medicationService;
    private final MedicationRepository medicationRepository;

    /**
     * Constructs a new DashboardServiceImpl with the required MedicationService dependency.
     *
     * @param medicationService The medication service used to retrieve medication data
     */
    public DashboardServiceImpl(MedicationService medicationService, MedicationRepository medicationRepository) {
        this.medicationService = medicationService;
        this.medicationRepository = medicationRepository;
        logger.debug("DashboardServiceImpl initialized with MedicationService");
    }

    /**
     * Retrieves and processes all dashboard data for the specified user.
     * This includes medication schedules, health conditions, metrics, and alerts.
     *
     * @param userDetails The authenticated user details
     * @return DashboardDTO containing all dashboard data for the user
     * @throws IllegalArgumentException if userDetails is null
     * @throws RuntimeException if there's an error processing dashboard data
     */
    @Override
    public DashboardDTO getUserDashboardData(UserDetails userDetails) {
        logger.debug("Entering getUserDashboardData for user: {}",
                userDetails != null ? userDetails.getUsername() : "null");

        if (userDetails == null) {
            logger.error("UserDetails parameter cannot be null");
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        String username = userDetails.getUsername();
        logger.info("Building dashboard data for user: {}", username);

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setUsername(username);

        try {
            // Retrieve and process medication schedule
            logger.debug("Retrieving medication schedule for user: {}", username);
            List<MedicationScheduleDTO> schedule = medicationService.getMedicationSchedule(username);
            logger.debug("Retrieved {} medication schedule entries for user: {}",
                    schedule.size(), username);

            List<DashboardDTO.UpcomingMedicationDto> upcomingMeds = processMedicationSchedule(schedule);
            logger.debug("Processed {} upcoming medications for user: {}",
                    upcomingMeds.size(), username);

            // Set all dashboard metrics
            logger.debug("Setting dashboard metrics for user: {}", username);
            dashboard.setActiveMedicationsCount(countActiveMedications(schedule));
            dashboard.setTodaysDosesCount(upcomingMeds.size());
            dashboard.setHealthMetricsCount(0); // Placeholder - would come from health service
            dashboard.setHasMedications(hasMedications(userDetails));
            dashboard.setUpcomingMedications(upcomingMeds);

            // Generate medication alerts
            logger.debug("Generating medication alerts for user: {}", username);
            dashboard.setAlerts(generateMedicationAlerts(schedule));

            logger.info("Successfully built dashboard for user: {}", username);
        } catch (Exception e) {
            logger.error("Error building dashboard for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to build dashboard data", e);
        }

        return dashboard;
    }

    /**
     * Processes the medication schedule to extract upcoming doses.
     * Filters medications with future schedule times and sorts them chronologically.
     *
     * @param schedule The list of medication schedule DTOs
     * @return List of upcoming medications sorted by schedule time
     */
    private List<DashboardDTO.UpcomingMedicationDto> processMedicationSchedule(List<MedicationScheduleDTO> schedule) {
        logger.debug("Processing medication schedule with {} entries",
                schedule != null ? schedule.size() : "null");

        if (schedule == null || schedule.isEmpty()) {
            logger.debug("Empty or null schedule provided, returning empty list");
            return Collections.emptyList();
        }

        LocalTime now = LocalTime.now();
        logger.debug("Current time for schedule filtering: {}", now);

        List<DashboardDTO.UpcomingMedicationDto> result = schedule.stream()
                .filter(med -> {
                    boolean isValid = med.getScheduleTime() != null && med.getScheduleTime().isAfter(now);
                    if (!isValid) {
                        logger.trace("Filtered out medication {} with schedule time {}",
                                med.getMedicationName(), med.getScheduleTime());
                    }
                    return isValid;
                })
                .sorted(Comparator.comparing(MedicationScheduleDTO::getScheduleTime))
                .map(this::convertToUpcomingMedicationDto)
                .collect(Collectors.toList());

        logger.debug("Processed {} upcoming medications from schedule", result.size());
        return result;
    }

    /**
     * Converts a MedicationScheduleDTO to an UpcomingMedicationDto for dashboard display.
     *
     * @param scheduleDto The medication schedule DTO to convert
     * @return UpcomingMedicationDto with relevant medication details
     */
    private DashboardDTO.UpcomingMedicationDto convertToUpcomingMedicationDto(MedicationScheduleDTO scheduleDto) {
        logger.debug("Converting MedicationScheduleDTO to UpcomingMedicationDto for medication: {}",
                scheduleDto.getMedicationName());

        DashboardDTO.UpcomingMedicationDto dto = new DashboardDTO.UpcomingMedicationDto();
        dto.setName(scheduleDto.getMedicationName());
        dto.setDosage(scheduleDto.getDosage());
        dto.setNextDoseTime(scheduleDto.getScheduleTime().toString());
        dto.setTaken(scheduleDto.isTaken());


        logger.trace("Converted medication details: name={}, dosage={}, time={}, taken={}",
                dto.getName(), dto.getDosage(), dto.getNextDoseTime(), dto.isTaken());

        return dto;
    }

    /**
     * Counts the number of distinct active medications in the schedule.
     *
     * @param schedule The list of medication schedule DTOs
     * @return Count of distinct active medications
     */
    private int countActiveMedications(List<MedicationScheduleDTO> schedule) {
        logger.debug("Counting active medications in schedule with {} entries",
                schedule != null ? schedule.size() : "null");

        if (schedule == null) {
            logger.debug("Null schedule provided, returning 0 active medications");
            return 0;
        }

        int count = (int) schedule.stream()
                .map(MedicationScheduleDTO::getMedicationId)
                .distinct()
                .count();

        logger.debug("Found {} distinct active medications", count);
        return count;
    }

    /**
     * Generates placeholder medication alerts for the dashboard.
     * In a production environment, this would check for actual refill needs,
     * interactions, and other medication-related alerts.
     *
     * @param schedule The list of medication schedule DTOs
     * @return List of generated medication alerts
     */
    private List<DashboardDTO.MedicationAlertDto> generateMedicationAlerts(List<MedicationScheduleDTO> schedule) {
        logger.debug("Generating medication alerts for schedule with {} entries",
                schedule != null ? schedule.size() : "null");

        List<DashboardDTO.MedicationAlertDto> alerts = new ArrayList<>();

        // Sample refill alert (would check actual refill status in production)
        if (!schedule.isEmpty()) {
            logger.trace("Adding placeholder refill alert");
            alerts.add(new DashboardDTO.MedicationAlertDto(
                    "Placeholder",
                    "Placeholder",
                    schedule.get(0).getMedicationName()
            ));
        }

        // Sample interaction alert (would check actual interactions in production)
        logger.trace("Adding placeholder interaction alert");
        alerts.add(new DashboardDTO.MedicationAlertDto(
                "Placeholder",
                "Placeholder",
                "Placeholder"
        ));

        logger.debug("Generated {} medication alerts", alerts.size());
        return alerts;
    }

    /**
     * Checks if the user has any medications in their schedule.
     *
     * @param userDetails The authenticated user details
     * @return true if the user has medications, false otherwise
     * @throws IllegalArgumentException if userDetails is null
     */
    @Override
    public boolean hasMedications(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }
        List<MedicationModel> user = medicationRepository.findByUserUsernameWithMedicationDetails(userDetails.getUsername());
        return user != null && !user.isEmpty();
    }

}