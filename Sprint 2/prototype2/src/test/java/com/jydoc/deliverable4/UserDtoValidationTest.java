package com.jydoc.deliverable4;

import com.jydoc.deliverable4.DTO.UserDTO;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

//NOTE: Passes all tests

@DisplayName("UserDTO Validation Tests")
class UserDtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("When user is valid")
    class ValidUserTests {
        @Test
        @DisplayName("Should pass all validations")
        void validUser_shouldPassValidation() {
            UserDTO user = createValidUser();
            assertNoViolations(user);
        }

        @Test
        @DisplayName("Should have default authority ROLE_USER")
        void shouldHaveDefaultAuthority() {
            UserDTO user = createValidUser();
            assertEquals("ROLE_USER", user.getAuthority());
        }
    }

    @Nested
    @DisplayName("Username validation")
    class UsernameValidation {
        @ParameterizedTest(name = "[{index}] ''{0}'' should fail validation")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should reject blank username")
        void blankUsername_shouldFail(String username) {
            UserDTO user = createValidUser();
            user.setUsername(username);
            assertHasViolationWithMessage(user, "Username is required");
        }

        @Test
        @DisplayName("Should reject username shorter than 3 chars")
        void tooShortUsername_shouldFail() {
            UserDTO user = createValidUser();
            user.setUsername("ab");
            assertHasViolationWithMessage(user, "Username must be 3-20 characters");
        }

        @Test
        @DisplayName("Should reject username longer than 20 chars")
        void tooLongUsername_shouldFail() {
            UserDTO user = createValidUser();
            user.setUsername("a".repeat(21));
            assertHasViolationWithMessage(user, "Username must be 3-20 characters");
        }
    }

    @Nested
    @DisplayName("Password validation")
    class PasswordValidation {
        @ParameterizedTest(name = "[{index}] ''{0}'' should fail validation")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should reject blank password")
        void blankPassword_shouldFail(String password) {
            UserDTO user = createValidUser();
            user.setPassword(password);
            assertHasViolationWithMessage(user, "Password is required");
        }

        @Test
        @DisplayName("Should reject password shorter than 6 chars")
        void tooShortPassword_shouldFail() {
            UserDTO user = createValidUser();
            user.setPassword("Short");
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(user);
            assertTrue(violations.stream().anyMatch(v ->
                    v.getMessage().equals("Password must be at least 6 characters")));
            assertTrue(violations.stream().anyMatch(v ->
                    v.getMessage().equals("Password must contain at least one uppercase, lowercase letter and number")));
        }

        @ParameterizedTest(name = "[{index}] ''{0}'' should fail pattern validation")
        @ValueSource(strings = {"nouppercase1", "NOLOWERCASE1", "NoNumbersHere"})
        @DisplayName("Should reject invalid password patterns")
        void invalidPatternPassword_shouldFail(String password) {
            UserDTO user = createValidUser();
            user.setPassword(password);
            assertHasViolationWithMessage(user,
                    "Password must contain at least one uppercase, lowercase letter and number");
        }
    }

    @Nested
    @DisplayName("Email validation")
    class EmailValidation {
        @ParameterizedTest(name = "[{index}] ''{0}'' should fail validation")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should reject blank email")
        void blankEmail_shouldFail(String email) {
            UserDTO user = createValidUser();
            user.setEmail(email);
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(user);
            assertTrue(violations.stream().anyMatch(v ->
                            v.getMessage().equals("Email is required")),
                    "Should have 'Email is required' violation");
        }

        @ParameterizedTest(name = "[{index}] ''{0}'' should fail format validation")
        @ValueSource(strings = {
                "plainstring",
                "missing@dot",
                "@domain.com",
                "user@.com",
                "user@domain..com",
                "user@domain.c",
                "user@domain,com",
                "user@domain_com"
        })
        @DisplayName("Should reject invalid email formats")
        void invalidFormatEmail_shouldFail(String email) {
            UserDTO user = createValidUser();
            user.setEmail(email);
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(user);

            // First check if there's a format violation
            boolean hasFormatViolation = violations.stream()
                    .anyMatch(v -> v.getMessage().equals("Invalid email format"));

            // If no format violation, check if it's being caught by the @NotBlank constraint
            if (!hasFormatViolation) {
                boolean hasBlankViolation = violations.stream()
                        .anyMatch(v -> v.getMessage().equals("Email is required"));
                assertTrue(hasBlankViolation,
                        "Expected either format violation or blank violation for: " + email);
            } else {
                assertTrue(true); // Format violation found as expected
            }
        }
    }

    @Nested
    @DisplayName("Name validation")
    class NameValidation {
        @ParameterizedTest(name = "[{index}] ''{0}'' should fail validation")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should reject blank first name")
        void blankFirstName_shouldFail(String firstName) {
            UserDTO user = createValidUser();
            user.setFirstName(firstName);
            assertHasViolationWithMessage(user, "First name is required");
        }

        @ParameterizedTest(name = "[{index}] ''{0}'' should fail validation")
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t", "\n"})
        @DisplayName("Should reject blank last name")
        void blankLastName_shouldFail(String lastName) {
            UserDTO user = createValidUser();
            user.setLastName(lastName);
            assertHasViolationWithMessage(user, "Last name is required");
        }
    }

    // Helper methods
    private UserDTO createValidUser() {
        UserDTO user = new UserDTO();
        user.setUsername("validUser123");
        user.setPassword("Valid1Password");
        user.setEmail("valid@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }

    private void assertNoViolations(UserDTO user) {
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(user);
        assertTrue(violations.isEmpty(),
                "Expected no violations but found: " + violations.size() + "\n" +
                        violations.stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .collect(Collectors.joining("\n")));
    }

    private void assertHasViolationWithMessage(UserDTO user, String expectedMessage) {
        Set<ConstraintViolation<UserDTO>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Expected at least one violation but found none");
        assertTrue(violations.stream()
                        .anyMatch(v -> v.getMessage().equals(expectedMessage)),
                "Expected violation with message: '" + expectedMessage +
                        "' but found:\n" + violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .collect(Collectors.joining("\n")));
    }
}