package com.jydoc.deliverable4.modeltests;

import com.jydoc.deliverable4.model.MedicationIntakeTime;
import com.jydoc.deliverable4.model.MedicationModel;
import jakarta.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationIntakeTimeTest {

    private MedicationIntakeTime intakeTime;

    @Mock
    private MedicationModel mockMedication;

    @BeforeEach
    void setUp() {
        intakeTime = MedicationIntakeTime.builder()
                .id(1L)
                .medication(mockMedication)
                .intakeTime(LocalTime.of(8, 0))
                .build();
    }

    @Test
    void testBuilderCreatesValidObject() {
        assertNotNull(intakeTime);
        assertEquals(1L, intakeTime.getId());
        assertEquals(mockMedication, intakeTime.getMedication());
        assertEquals(LocalTime.of(8, 0), intakeTime.getIntakeTime());
    }

    @Test
    void testConstructorWithParameters() {
        LocalTime time = LocalTime.of(12, 30);
        MedicationIntakeTime newIntakeTime = new MedicationIntakeTime(mockMedication, time);

        assertEquals(mockMedication, newIntakeTime.getMedication());
        assertEquals(time, newIntakeTime.getIntakeTime());
        assertNull(newIntakeTime.getId()); // ID not set by constructor
    }

    @Test
    void testEqualsAndHashCode() {
        MedicationIntakeTime sameIntakeTime = MedicationIntakeTime.builder()
                .id(2L) // Different ID shouldn't matter for equality
                .medication(mockMedication)
                .intakeTime(LocalTime.of(8, 0))
                .build();

        MedicationIntakeTime differentTime = MedicationIntakeTime.builder()
                .medication(mockMedication)
                .intakeTime(LocalTime.of(9, 0))
                .build();

        MedicationIntakeTime differentMedication = MedicationIntakeTime.builder()
                .medication(mock(MedicationModel.class)) // Different medication
                .intakeTime(LocalTime.of(8, 0))
                .build();

        // Test equality
        assertEquals(intakeTime, sameIntakeTime);
        assertEquals(intakeTime.hashCode(), sameIntakeTime.hashCode());

        // Test inequality
        assertNotEquals(intakeTime, differentTime);
        assertNotEquals(intakeTime, differentMedication);
        assertNotEquals(intakeTime, null);
        assertNotEquals(intakeTime, new Object());
    }

    @Test
    void testSetters() {
        LocalTime newTime = LocalTime.of(14, 0);
        MedicationModel newMedication = mock(MedicationModel.class);

        intakeTime.setIntakeTime(newTime);
        intakeTime.setMedication(newMedication);

        assertEquals(newTime, intakeTime.getIntakeTime());
        assertEquals(newMedication, intakeTime.getMedication());
    }

    @Test
    void testNullChecks() {
        assertThrows(NullPointerException.class, () -> new MedicationIntakeTime(null, LocalTime.now()));
        assertThrows(NullPointerException.class, () -> intakeTime.setMedication(null));
        assertThrows(NullPointerException.class, () -> intakeTime.setIntakeTime(null));
    }

    @Test
    void testJpaRelationshipManagement() throws NoSuchFieldException {
        // Verify the @ManyToOne relationship is properly configured
        ManyToOne manyToOne = MedicationIntakeTime.class
                .getDeclaredField("medication")
                .getAnnotation(ManyToOne.class);

        assertNotNull(manyToOne);
        assertEquals(FetchType.LAZY, manyToOne.fetch());

        JoinColumn joinColumn = MedicationIntakeTime.class
                .getDeclaredField("medication")
                .getAnnotation(JoinColumn.class);

        assertNotNull(joinColumn);
        assertEquals("medication_id", joinColumn.name());
        assertFalse(joinColumn.nullable());
    }

    @Test
    void testTableConfiguration() {
        Table table = MedicationIntakeTime.class.getAnnotation(Table.class);
        assertNotNull(table);
        assertEquals("medication_intake_times", table.name());
    }

    @Test
    void testIdGenerationStrategy() throws NoSuchFieldException {
        GeneratedValue generatedValue = MedicationIntakeTime.class
                .getDeclaredField("id")
                .getAnnotation(GeneratedValue.class);

        assertNotNull(generatedValue);
        assertEquals(GenerationType.IDENTITY, generatedValue.strategy());
    }
}