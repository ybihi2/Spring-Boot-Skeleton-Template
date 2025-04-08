package com.jydoc.deliverable4.services.impl;

import com.jydoc.deliverable4.dtos.MedicationScheduleDTO;
import com.jydoc.deliverable4.dtos.userdtos.DashboardDTO;
import com.jydoc.deliverable4.services.userservices.DashboardService;
import com.jydoc.deliverable4.services.medicationservices.MedicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {
    private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class);

    private final MedicationService medicationService;

    public DashboardServiceImpl(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @Override
    public DashboardDTO getUserDashboardData(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        String username = userDetails.getUsername();
        logger.info("Building dashboard data for user: {}", username);

        DashboardDTO dashboard = new DashboardDTO();
        dashboard.setUsername(username);

        try {
            // Set health conditions (in a real app, this would come from a health service)
            dashboard.setHealthConditions(Arrays.asList("Hypertension", "Type 2 Diabetes"));

            // Get and process medication data
            List<MedicationScheduleDTO> schedule = medicationService.getMedicationSchedule(username);
            List<DashboardDTO.UpcomingMedicationDto> upcomingMeds = processMedicationSchedule(schedule);

            // Set dashboard metrics
            dashboard.setActiveMedicationsCount(countActiveMedications(schedule));
            dashboard.setTodaysDosesCount(upcomingMeds.size());
            dashboard.setHealthMetricsCount(3); // Placeholder - would come from health service
            dashboard.setHasMedications(!schedule.isEmpty());
            dashboard.setUpcomingMedications(upcomingMeds);

            // Generate alerts (in a real app, these would come from an alert service)
            dashboard.setAlerts(generateMedicationAlerts(schedule));

        } catch (Exception e) {
            logger.error("Error building dashboard for user {}: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to build dashboard data", e);
        }

        return dashboard;
    }

    private List<DashboardDTO.UpcomingMedicationDto> processMedicationSchedule(List<MedicationScheduleDTO> schedule) {
        if (schedule == null || schedule.isEmpty()) {
            return Collections.emptyList();
        }

        return schedule.stream()
                .filter(med -> med.getScheduleTime() != null && med.getScheduleTime().isAfter(LocalTime.now()))
                .sorted(Comparator.comparing(MedicationScheduleDTO::getScheduleTime))
                .map(this::convertToUpcomingMedicationDto)
                .collect(Collectors.toList());
    }

    private DashboardDTO.UpcomingMedicationDto convertToUpcomingMedicationDto(MedicationScheduleDTO scheduleDto) {
        DashboardDTO.UpcomingMedicationDto dto = new DashboardDTO.UpcomingMedicationDto();
        dto.setName(scheduleDto.getMedicationName());
        dto.setDosage(scheduleDto.getDosage());
        dto.setNextDoseTime(scheduleDto.getScheduleTime().toString());
        dto.setTaken(scheduleDto.isTaken());
        return dto;
    }

    private int countActiveMedications(List<MedicationScheduleDTO> schedule) {
        if (schedule == null) return 0;
        return (int) schedule.stream()
                .map(MedicationScheduleDTO::getMedicationId)
                .distinct()
                .count();
    }

    private List<DashboardDTO.MedicationAlertDto> generateMedicationAlerts(List<MedicationScheduleDTO> schedule) {
        List<DashboardDTO.MedicationAlertDto> alerts = new ArrayList<>();

        // Sample refill alert (in real app, would check actual refill status)
        if (!schedule.isEmpty()) {
            alerts.add(new DashboardDTO.MedicationAlertDto(
                    "Refill",
                    "Your medication 'Lisinopril' needs a refill soon",
                    "Lisinopril"
            ));
        }

        // Sample interaction alert
        alerts.add(new DashboardDTO.MedicationAlertDto(
                "Interaction",
                "Potential interaction between Metformin and Ibuprofen",
                "Metformin"
        ));

        return alerts;
    }

    @Override
    public boolean hasMedications(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails cannot be null");
        }

        try {
            List<MedicationScheduleDTO> schedule = medicationService.getMedicationSchedule(userDetails.getUsername());
            return schedule != null && !schedule.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking medications for user {}: {}", userDetails.getUsername(), e.getMessage());
            return false;
        }
    }
}