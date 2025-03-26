package com.jydoc.deliverable3;

import com.jydoc.deliverable3.Model.UserModel;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Deliverable3ApplicationTests {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {}, "Context should load successfully");
    }

    @Test
    void testValidUserModel() {
        UserModel validUser = new UserModel();
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setEmail("john.doe@example.com");
        validUser.setPassword("Password123");
        validUser.setAdmin(false);

        Set<ConstraintViolation<UserModel>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Valid user should have no violations");
    }

    @Test
    void testFirstNameValidations() {
        UserModel user = new UserModel();
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setPassword("Password123");

        // Test first name too short
        user.setFirstName("A");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "First name less than 2 characters should be invalid");

        // Test first name too long
        user.setFirstName("ThisIsAVeryLongFirstNameThatExceedsFiftyCharactersLimit");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "First name over 50 characters should be invalid");

        // Test valid first name
        user.setFirstName("John");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid first name should pass");
    }

    @Test
    void testLastNameValidations() {
        UserModel user = new UserModel();
        user.setFirstName("John");
        user.setEmail("test@example.com");
        user.setPassword("Password123");

        // Test last name too short
        user.setLastName("A");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Last name less than 2 characters should be invalid");

        // Test last name too long
        user.setLastName("ThisIsAVeryLongLastNameThatExceedsFiftyCharactersLimit");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Last name over 50 characters should be invalid");

        // Test valid last name
        user.setLastName("Doe");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid last name should pass");
    }

    @Test
    void testEmailValidations() {
        UserModel user = new UserModel();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("Password123");

        // Test blank email
        user.setEmail("");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Blank email should be invalid");

        // Test invalid email format
        user.setEmail("invalid-email");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Invalid email format should be rejected");

        // Test valid email
        user.setEmail("valid.email@example.com");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid email should pass");
    }

    @Test
    void testPasswordValidations() {
        UserModel user = new UserModel();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");

        // Test password too short
        user.setPassword("Pwd1");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Password less than 6 characters should be invalid");

        // Test password without numbers
        user.setPassword("Password");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Password without numbers should be invalid");

        // Test password without letters
        user.setPassword("123456");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Password without letters should be invalid");

        // Test valid password
        user.setPassword("Password123");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid password should pass");
    }

    @Test
    void testAdminFlag() {
        UserModel user = new UserModel();
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setEmail("admin@example.com");
        user.setPassword("Admin123");

        user.setAdmin(true);
        assertTrue(user.isAdmin(), "Admin flag should be settable to true");

        user.setAdmin(false);
        assertFalse(user.isAdmin(), "Admin flag should be settable to false");
    }
}