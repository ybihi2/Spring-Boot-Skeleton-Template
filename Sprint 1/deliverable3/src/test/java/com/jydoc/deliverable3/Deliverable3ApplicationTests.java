package com.jydoc.deliverable3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class Deliverable3ApplicationTests {

   @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUserModel() {
        UserModel validUser = new UserModel();
        validUser.setFirstName("John");
        validUser.setLastName("Doe");
        validUser.setEmail("john.doe@example.com");
        validUser.setPassword("password123");
        validUser.setAdmin(false);

        Set<ConstraintViolation<UserModel>> violations = validator.validate(validUser);
        assertTrue(violations.isEmpty(), "Valid user should have no violations");
    }

    @Test
    void testFirstNameValidations() {
        UserModel user = new UserModel();
        
        // Test first name too short
        user.setFirstName("A");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "First name less than 2 characters are invalid.");

        // Test first name too long
        user.setFirstName("ThisIsAVeryLongFirstNameThatExceedsFiftyCharactersLimit");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "First name over 50 characters are invalid.");

        // Test first name with invalid characters
        user.setFirstName("John123");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "First name with numbers are invalid.");

        // Test first name with allowed special characters
        user.setFirstName("Mary-Jane");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "First names with a hyphen are valid.");

        user.setFirstName("O'Brien");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "First name with apostrophe should be valid.");
    }

    @Test
    void testLastNameValidations() {
        UserModel user = new UserModel();
        
        // Test last name too short
        user.setLastName("A");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Last name less than 2 characters are invalid.");

        // Test last name too long
        user.setLastName("ThisIsAVeryLongLastNameThatExceedsFiftyCharactersLimit");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Last name over 50 characters are invalid.");

        // Test last name with invalid characters
        user.setLastName("Smith123");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Last name with numbers are invalid.");

        // Test last name with allowed special characters
        user.setLastName("Van-Helsing");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Last name with a hyphen should be valid.");
    }

    @Test
    void testEmailValidations() {
        UserModel user = new UserModel();
        
        // Test blank email
        user.setEmail("");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "A blank email is invalid.");

        // Test invalid email formats
        String[] invalidEmails = {
            "invalid-email",
            "invalid@email",
            "invalid@email.",
            "@email.com",
            "email@.com"
        };

        for (String email : invalidEmails) {
            user.setEmail(email);
            violations = validator.validate(user);
            assertFalse(violations.isEmpty(), "Invalid email format: " + email);
        }

        // Test valid email
        user.setEmail("valid.email123@example.com");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid email should pass.");
    }

    @Test
    void testPasswordValidations() {
        UserModel user = new UserModel();
        
        // Test password too short
        user.setPassword("12");
        Set<ConstraintViolation<UserModel>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Passwords less than 6 characters are invalid.");

        // Test password without letters
        user.setPassword("123456");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Passwords without letters are invalid.");

        // Test password without numbers
        user.setPassword("password");
        violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Passwords without numbers are invalid.l");

        // Test valid password
        user.setPassword("password123");
        violations = validator.validate(user);
        assertTrue(violations.isEmpty(), "Valid passwords will pass.");
    }

    @Test
    void testAdminFlag() {
        UserModel adminUser = new UserModel();
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setPassword("admin123");
        adminUser.setAdmin(true);

        UserModel regularUser = new UserModel();
        regularUser.setFirstName("Regular");
        regularUser.setLastName("User");
        regularUser.setEmail("regular@example.com");
        regularUser.setPassword("regular123");
        regularUser.setAdmin(false);

        // Verify admin flag can be set correctly
        assertTrue(adminUser.isAdmin(), "Admin flag should be settable to true.");
        assertFalse(regularUser.isAdmin(), "Admin flag should be settable to false.");
    }
}

	@Test
	void contextLoads() {
	}

}
