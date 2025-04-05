package com.jydoc.deliverable4;

import com.jydoc.deliverable4.DTO.LoginDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LoginDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // Test data providers
    static Stream<String> validUsernames() {
        return Stream.of("user123", "admin", "test.user@example.com", "a".repeat(100));
    }

    static Stream<String> validPasswords() {
        return Stream.of("password123", "P@ssw0rd", "a".repeat(128), "12345678");
    }

    // Constructor tests
    @Test
    void constructor_ShouldCreateInstanceWithProvidedValues() {
        String testUsername = "testUser";
        String testPassword = "securePassword123";

        LoginDTO loginDTO = new LoginDTO(testUsername, testPassword);

        assertEquals(testUsername, loginDTO.username());
        assertEquals(testPassword, loginDTO.password());
    }

    // Validation tests
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void usernameValidation_ShouldFailForBlankValues(String invalidUsername) {
        LoginDTO loginDTO = new LoginDTO(invalidUsername, "validPassword123");
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        assertFalse(violations.isEmpty());
        assertEquals("Username or email cannot be blank", violations.iterator().next().getMessage());
    }

    @ParameterizedTest
    @MethodSource("validUsernames")
    void usernameValidation_ShouldPassForValidValues(String validUsername) {
        LoginDTO loginDTO = new LoginDTO(validUsername, "validPassword123");
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void passwordValidation_ShouldFailForBlankValues(String invalidPassword) {
        LoginDTO loginDTO = new LoginDTO("validUser", invalidPassword);
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        assertFalse(violations.isEmpty(), "Should have violations for blank password");

        // Check that at least one of the expected messages is present
        boolean hasBlankViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password cannot be blank"));
        boolean hasSizeViolation = violations.stream()
                .anyMatch(v -> v.getMessage().equals("Password must be at least 8 characters long"));

        // For blank values, we should get EITHER the blank message OR the size message
        assertTrue(hasBlankViolation || hasSizeViolation,
                "Should have either blank or size violation for empty password");
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "1234567"})
    void passwordValidation_ShouldFailForShortPasswords(String shortPassword) {
        LoginDTO loginDTO = new LoginDTO("validUser", shortPassword);
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        assertFalse(violations.isEmpty());
        assertEquals("Password must be at least 8 characters long", violations.iterator().next().getMessage());
    }

    @ParameterizedTest
    @MethodSource("validPasswords")
    void passwordValidation_ShouldPassForValidValues(String validPassword) {
        LoginDTO loginDTO = new LoginDTO("validUser", validPassword);
        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        assertTrue(violations.isEmpty());
    }

    // empty() method tests
    @Test
    void empty_ShouldCreateInstanceWithBlankCredentials() {
        LoginDTO emptyDTO = LoginDTO.empty();

        assertTrue(emptyDTO.username().isEmpty());
        assertTrue(emptyDTO.password().isEmpty());
    }

    // getNormalizedUsername() tests
    @Test
    void getNormalizedUsername_ShouldTrimWhitespace() {
        LoginDTO loginDTO = new LoginDTO("  testUser  ", "password");

        assertEquals("testuser", loginDTO.getNormalizedUsername());
    }

    @Test
    void getNormalizedUsername_ShouldConvertToLowerCase() {
        LoginDTO loginDTO = new LoginDTO("TestUser", "password");

        assertEquals("testuser", loginDTO.getNormalizedUsername());
    }

    @Test
    void getNormalizedUsername_ShouldHandleEmptyString() {
        LoginDTO loginDTO = new LoginDTO("", "password");

        assertEquals("", loginDTO.getNormalizedUsername());
    }

    // isEmpty() tests
    @Test
    void isEmpty_ShouldReturnTrueForEmptyInstance() {
        LoginDTO emptyDTO = LoginDTO.empty();

        assertTrue(emptyDTO.isEmpty());
    }

    @Test
    void isEmpty_ShouldReturnFalseForNonEmptyUsername() {
        LoginDTO loginDTO = new LoginDTO("user", "");

        assertFalse(loginDTO.isEmpty());
    }

    @Test
    void isEmpty_ShouldReturnFalseForNonEmptyPassword() {
        LoginDTO loginDTO = new LoginDTO("", "password");

        assertFalse(loginDTO.isEmpty());
    }

    @Test
    void isEmpty_ShouldReturnFalseForFullyPopulatedInstance() {
        LoginDTO loginDTO = new LoginDTO("user", "password");

        assertFalse(loginDTO.isEmpty());
    }

    // Record component tests
    @Test
    void recordComponents_ShouldBeAccessible() {
        LoginDTO loginDTO = new LoginDTO("test", "password");

        assertEquals("test", loginDTO.username());
        assertEquals("password", loginDTO.password());
    }
}