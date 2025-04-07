package com.jydoc.deliverable4.services.impl;

import com.jydoc.deliverable4.dtos.DashboardDTO;
import com.jydoc.deliverable4.model.MedicationModel;
import com.jydoc.deliverable4.services.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Service implementation for dashboard-related operations.
 * <p>
 * This service provides methods to retrieve and manage dashboard data for users,
 * including medication information, alerts, and health status.
 * </p>
 */
@Service
public class DashboardServiceImpl implements DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    /**
     * Retrieves comprehensive dashboard data for the authenticated user.
     * <p>
     * This method constructs a complete dashboard view including user information,
     * health conditions, medication status, alerts, and upcoming medications.
     * </p>
     *
     * @param userDetails The authenticated user details
     * @return A fully populated DashboardDTO object
     * @throws IllegalArgumentException if userDetails is null
     */
    @Override
    public DashboardDTO getUserDashboardData(UserDetails userDetails) {
        logger.info("Building dashboard data for user: {}",
                userDetails != null ? userDetails.getUsername() : "null");

        if (userDetails == null) {
            logger.error("UserDetails parameter cannot be null");
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        DashboardDTO dashboard = new DashboardDTO();

        try {
            // Set user info
            dashboard.setUsername(userDetails.getUsername());
            logger.debug("Set username for dashboard: {}", userDetails.getUsername());

            // Set health conditions (currently hardcoded for demo)
            List<String> healthConditions = Arrays.asList("Hypertension", "Type 2 Diabetes");
            dashboard.setHealthConditions(healthConditions);
            logger.debug("Set health conditions: {}", healthConditions);

            // Set medication status (currently hardcoded to trigger reminders)
            dashboard.setActiveMedicationsCount(0);
            dashboard.setHasMedications(false);
            logger.debug("Set medication status to trigger reminders");

            // Create alerts (currently hardcoded for demo)
            List<DashboardDTO.MedicationAlertDto> alerts = createDemoAlerts();
            dashboard.setAlerts(alerts);
            logger.debug("Added {} alerts to dashboard", alerts.size());

            // Upcoming medication (empty to trigger reminder)
            dashboard.setUpcomingMedications(new ArrayList<>());
            logger.debug("Set empty upcoming medications to trigger reminder");

        } catch (Exception e) {
            logger.error("Error building dashboard data for user {}: {}",
                    userDetails.getUsername(), e.getMessage(), e);
            throw e;
        }

        logger.info("Successfully built dashboard for user: {}", userDetails.getUsername());
        return dashboard;
    }

    /**
     * Updates intake times for a medication.
     * <p>
     * Synchronizes the medication's intake times with the provided set by:
     * 1. Removing times not present in the new set
     * 2. Adding new times that don't currently exist
     * </p>
     *
     * @param medication The medication to update
     * @param newIntakeTimes The new set of intake times
     * @throws IllegalArgumentException if either parameter is null
     */
    private void updateIntakeTimes(MedicationModel medication, Set<LocalTime> newIntakeTimes) {
        logger.debug("Updating intake times for medication ID: {}",
                medication != null ? medication.getId() : "null");

        if (medication == null || newIntakeTimes == null) {
            String errorMsg = "Neither medication nor newIntakeTimes can be null";
            logger.error(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        try {
            // 1. Remove times not in the new set
            Set<LocalTime> currentTimes = medication.getIntakeTimesAsLocalTimes();
            long removedCount = currentTimes.stream()
                    .filter(time -> !newIntakeTimes.contains(time))
                    .peek(time -> logger.trace("Removing intake time: {}", time))
                    .peek(medication::removeIntakeTime)
                    .count();

            logger.debug("Removed {} intake times", removedCount);

            // 2. Add new times that don't exist
            long addedCount = newIntakeTimes.stream()
                    .filter(time -> !currentTimes.contains(time))
                    .peek(time -> logger.trace("Adding new intake time: {}", time))
                    .peek(medication::addIntakeTime)
                    .count();

            logger.debug("Added {} new intake times", addedCount);
        } catch (Exception e) {
            logger.error("Error updating intake times for medication ID {}: {}",
                    medication.getId(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Checks if the user has any medications.
     * <p>
     * In a real implementation, this would query the database for the user's medications.
     * Currently returns false to trigger reminders in the UI.
     * </p>
     *
     * @param userDetails The authenticated user details
     * @return false (hardcoded for demo purposes to trigger reminders)
     * @throws IllegalArgumentException if userDetails is null
     */
    @Override
    public boolean hasMedications(UserDetails userDetails) {
        logger.debug("Checking if user has medications: {}",
                userDetails != null ? userDetails.getUsername() : "null");

        if (userDetails == null) {
            logger.error("UserDetails parameter cannot be null");
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        // Currently hardcoded to return false to trigger reminder
        logger.debug("Returning false to trigger medication reminder");
        return false;
    }

    /**
     * Creates demo alerts for the dashboard.
     * <p>
     * This is a temporary implementation that returns hardcoded alerts.
     * In a real implementation, these would be generated based on actual data.
     * </p>
     *
     * @return List of demo MedicationAlertDto objects
     */
    private List<DashboardDTO.MedicationAlertDto> createDemoAlerts() {
        logger.debug("Creating demo alerts");
        List<DashboardDTO.MedicationAlertDto> alerts = new ArrayList<>();

        alerts.add(new DashboardDTO.MedicationAlertDto(
                "Refill",
                "Your medication 'Lisinopril' needs a refill soon",
                "Lisinopril"
        ));
        alerts.add(new DashboardDTO.MedicationAlertDto(
                "Interaction",
                "Potential interaction between Metformin and Ibuprofen",
                "Metformin"
        ));

        logger.debug("Created {} demo alerts", alerts.size());
        return alerts;
    }
}