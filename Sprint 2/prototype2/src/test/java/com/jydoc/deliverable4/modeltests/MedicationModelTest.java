package com.jydoc.deliverable4.modeltests;

import com.jydoc.deliverable4.dtos.MedicationDTO;
import com.jydoc.deliverable4.model.MedicationIntakeTime;
import com.jydoc.deliverable4.model.MedicationModel;
import com.jydoc.deliverable4.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class MedicationModelTest {

    private MedicationModel medicationModel;

    @Mock
    private UserModel mockUser;

    @BeforeEach
    void setUp() {
        medicationModel = MedicationModel.builder()
                .id(1L)
                .user(mockUser)
                .name("Test Medication")
                .urgency(MedicationModel.MedicationUrgency.ROUTINE)
                .intakeTimes(new HashSet<>())
                .dosage("500mg")
                .instructions("Take with food")
                .isActive(true)
                .build();
        // Remove the stubbing from here
    }

    @Test
    void testToDto() {
        // Stub only when needed
        when(mockUser.getId()).thenReturn(100L);

        LocalTime time1 = LocalTime.of(8, 0);
        medicationModel.addIntakeTime(time1);

        MedicationDTO dto = medicationModel.toDto();
        assertEquals(100L, dto.getUserId());
    }

    @Test
    void testAddIntakeTime() {
        LocalTime testTime = LocalTime.of(8, 0);
        medicationModel.addIntakeTime(testTime);

        assertEquals(1, medicationModel.getIntakeTimes().size());
        assertTrue(medicationModel.getIntakeTimesAsLocalTimes().contains(testTime));
    }

    @Test
    void testAddDuplicateIntakeTime() {
        LocalTime testTime = LocalTime.of(12, 0);
        medicationModel.addIntakeTime(testTime);
        medicationModel.addIntakeTime(testTime); // duplicate

        assertEquals(1, medicationModel.getIntakeTimes().size());
    }


    @Test
    void testAddNullIntakeTime() {
        assertThrows(NullPointerException.class, () -> {
            medicationModel.addIntakeTime(null);
        });
        assertTrue(medicationModel.getIntakeTimes().isEmpty());
    }

    @Test
    void testRemoveIntakeTime() {
        LocalTime testTime = LocalTime.of(20, 0);
        medicationModel.addIntakeTime(testTime); // Should be the same variable name
        medicationModel.removeIntakeTime(testTime); // Should be the same variable name

        assertTrue(medicationModel.getIntakeTimes().isEmpty()); // Fix the typo here
    }

    @Test
    void testRemoveNonExistentIntakeTime() {
        medicationModel.addIntakeTime(LocalTime.of(9, 0));
        medicationModel.removeIntakeTime(LocalTime.of(10, 0));

        assertEquals(1, medicationModel.getIntakeTimes().size());
    }

    @Test
    void testRemoveNullIntakeTime() {
        medicationModel.addIntakeTime(LocalTime.of(7, 0));

        assertThrows(NullPointerException.class, () -> {
            medicationModel.removeIntakeTime(null);
        });

        // Verify nothing was removed
        assertEquals(1, medicationModel.getIntakeTimes().size());
    }

    @Test
    void testGetIntakeTimesAsLocalTimes() {
        LocalTime time1 = LocalTime.of(8, 0);
        LocalTime time2 = LocalTime.of(12, 0);
        LocalTime time3 = LocalTime.of(18, 0);

        medicationModel.addIntakeTime(time1);
        medicationModel.addIntakeTime(time2);
        medicationModel.addIntakeTime(time3);

        Set<LocalTime> times = medicationModel.getIntakeTimesAsLocalTimes();

        assertEquals(3, times.size());
        assertTrue(times.contains(time1));
        assertTrue(times.contains(time2));
        assertTrue(times.contains(time3));
    }

    @Test
    void testGetIntakeTimesAsLocalTimesWithNullValues() {
        // Verify that null intake times are rejected
        assertThrows(IllegalArgumentException.class, () -> {
            new MedicationIntakeTime(medicationModel, null);
        });

        // Test that the method handles empty sets
        Set<LocalTime> times = medicationModel.getIntakeTimesAsLocalTimes();
        assertTrue(times.isEmpty());
    }



    @Test
    void testIntakeTimesHandling() {
        // Test that initially intakeTimes is not null and empty
        assertNotNull(medicationModel.getIntakeTimes());
        assertTrue(medicationModel.getIntakeTimes().isEmpty());

        // Test adding times works
        LocalTime time = LocalTime.of(8, 0);
        medicationModel.addIntakeTime(time);
        assertFalse(medicationModel.getIntakeTimes().isEmpty());
    }

    @Test
    void testAddIntakeTimeMaintainsBidirectionalRelationship() {
        LocalTime testTime = LocalTime.of(9, 0);

        // This creates and adds a new MedicationIntakeTime with proper bidirectional relationship
        medicationModel.addIntakeTime(testTime);

        // Verify the relationship through the public API
        assertFalse(medicationModel.getIntakeTimes().isEmpty(), "Should have one intake time");

        MedicationIntakeTime addedIntake = medicationModel.getIntakeTimes().iterator().next();
        assertEquals(testTime, addedIntake.getIntakeTime(), "Time should match");
        assertEquals(medicationModel, addedIntake.getMedication(), "Medication reference should match");
    }


    @Test
    void testToDtoWithNullUrgency() {
        medicationModel.setUrgency(null);
        MedicationDTO dto = medicationModel.toDto();
        assertNull(dto.getUrgency());
    }

    @Test
    void testEqualsAndHashCode() {
        MedicationModel model1 = MedicationModel.builder()
                .id(1L)
                .user(mockUser)
                .name("Med1")
                .urgency(MedicationModel.MedicationUrgency.ROUTINE)
                .build();

        MedicationModel model2 = MedicationModel.builder()
                .id(1L)
                .user(mockUser)
                .name("Med2") // different name
                .urgency(MedicationModel.MedicationUrgency.URGENT) // different urgency
                .build();

        MedicationModel model3 = MedicationModel.builder()
                .id(2L) // different ID
                .user(mockUser)
                .name("Med1")
                .urgency(MedicationModel.MedicationUrgency.ROUTINE)
                .build();

        assertEquals(model1, model2); // only ID matters for equality
        assertNotEquals(model1, model3);
        assertEquals(model1.hashCode(), model2.hashCode());
        assertNotEquals(model1.hashCode(), model3.hashCode());
    }

    @Test
    void testBuilder() {
        MedicationModel model = MedicationModel.builder()
                .id(2L)
                .user(mockUser)
                .name("Builder Test")
                .urgency(MedicationModel.MedicationUrgency.NONURGENT)
                .dosage("100mg")
                .instructions("Before sleep")
                .isActive(false)
                .build();

        assertEquals(2L, model.getId());
        assertEquals(mockUser, model.getUser());
        assertEquals("Builder Test", model.getName());
        assertEquals(MedicationModel.MedicationUrgency.NONURGENT, model.getUrgency());
        assertEquals("100mg", model.getDosage());
        assertEquals("Before sleep", model.getInstructions());
        assertFalse(model.getIsActive());
    }

    @Test
    void testDefaultIsActive() {
        MedicationModel model = new MedicationModel();
        assertTrue(model.getIsActive());
    }

    @Test
    void testDefaultIntakeTimes() {
        MedicationModel model = new MedicationModel();
        assertNotNull(model.getIntakeTimes());
        assertTrue(model.getIntakeTimes().isEmpty());
    }

    @Test
    void testMedicationUrgencyValues() {
        assertEquals(3, MedicationModel.MedicationUrgency.values().length);
        assertEquals(MedicationModel.MedicationUrgency.URGENT, MedicationModel.MedicationUrgency.valueOf("URGENT"));
        assertEquals(MedicationModel.MedicationUrgency.NONURGENT, MedicationModel.MedicationUrgency.valueOf("NONURGENT"));
        assertEquals(MedicationModel.MedicationUrgency.ROUTINE, MedicationModel.MedicationUrgency.valueOf("ROUTINE"));
    }
}