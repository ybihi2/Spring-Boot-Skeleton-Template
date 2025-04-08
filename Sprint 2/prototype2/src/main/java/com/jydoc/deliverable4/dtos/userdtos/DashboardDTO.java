package com.jydoc.deliverable4.dtos.userdtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class DashboardDTO {


    private String username;
    private List<String> healthConditions;
    private int activeMedicationsCount;
    private int todaysDosesCount;
    private int healthMetricsCount;
    private List<MedicationAlertDto> alerts;
    private List<UpcomingMedicationDto> upcomingMedications;
    private boolean hasMedications;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationAlertDto {
        private String type;
        private String message;
        private String medicationName;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpcomingMedicationDto {
        private String name;
        private String dosage;
        private String nextDoseTime;
        private boolean taken;
    }
}