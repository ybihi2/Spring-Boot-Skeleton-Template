package com.jydoc.deliverable4;

import com.jydoc.deliverable4.model.AuthorityModel;
import com.jydoc.deliverable4.model.UserModel;
import com.jydoc.deliverable4.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private UserModel testUser;
    private AuthorityModel testAuthority;

    @BeforeEach
    void setUp() {
        testAuthority = new AuthorityModel();
        testAuthority.setAuthority("ROLE_USER");
        entityManager.persist(testAuthority);

        testUser = UserModel.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .firstName("Test")    // Added
                .lastName("User")     // Added
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .authorities(Collections.singleton(testAuthority))
                .build();
        entityManager.persist(testUser);
        entityManager.flush();
    }

    // Basic CRUD operations
    @Test
    void whenFindById_thenReturnUser() {
        Optional<UserModel> foundUser = userRepository.findById(testUser.getId());
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getUsername(), foundUser.get().getUsername());
        assertEquals("Test", foundUser.get().getFirstName());  // Added
        assertEquals("User", foundUser.get().getLastName());   // Added
    }

    @Test
    void whenSaveNewUser_thenUserIsPersisted() {
        UserModel newUser = UserModel.builder()
                .username("newuser")
                .password("newpass")
                .email("new@example.com")
                .firstName("New")    // Added
                .lastName("User")    // Added
                .build();

        UserModel savedUser = userRepository.save(newUser);
        assertNotNull(savedUser.getId());
        assertEquals(newUser.getUsername(), savedUser.getUsername());
        assertEquals("New", savedUser.getFirstName());  // Added
        assertEquals("User", savedUser.getLastName());  // Added
    }

    // Unique constraint tests
    @Test
    void whenSaveDuplicateUsername_thenThrowException() {
        UserModel duplicateUser = UserModel.builder()
                .username("testuser")  // duplicate username
                .password("password")
                .email("different@example.com")
                .firstName("Diff")    // Added
                .lastName("User")     // Added
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    @Test
    void whenSaveDuplicateEmail_thenThrowException() {
        UserModel duplicateUser = UserModel.builder()
                .username("differentuser")
                .password("password")
                .email("test@example.com")  // duplicate email
                .firstName("Diff")    // Added
                .lastName("User")     // Added
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(duplicateUser);
        });
    }

    // Query method tests
    @Test
    void whenFindByUsername_thenReturnUser() {
        Optional<UserModel> foundUser = userRepository.findByUsername("testuser");
        assertTrue(foundUser.isPresent());
        assertEquals(testUser.getEmail(), foundUser.get().getEmail());
        assertEquals("Test", foundUser.get().getFirstName());  // Added
        assertEquals("User", foundUser.get().getLastName());   // Added
    }

    @Test
    void whenFindByNonExistentUsername_thenReturnEmpty() {
        Optional<UserModel> foundUser = userRepository.findByUsername("nonexistent");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void whenExistsByUsername_thenReturnCorrectBoolean() {
        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void whenExistsByEmail_thenReturnCorrectBoolean() {
        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    // Custom query tests
    @Test
    void whenFindByUsernameWithAuthorities_thenReturnUserWithAuthorities() {
        Optional<UserModel> foundUser = userRepository.findByUsernameWithAuthorities("testuser");
        assertTrue(foundUser.isPresent());
        assertFalse(foundUser.get().getAuthorities().isEmpty());
        assertEquals("ROLE_USER", foundUser.get().getAuthorities().iterator().next().getAuthority());
        assertEquals("Test", foundUser.get().getFirstName());  // Added
        assertEquals("User", foundUser.get().getLastName());   // Added
    }

    @Test
    void whenFindByUsernameOrEmail_thenReturnUser() {
        // Test with username
        Optional<UserModel> byUsername = userRepository.findByUsernameOrEmail("testuser");
        assertTrue(byUsername.isPresent());
        assertEquals("Test", byUsername.get().getFirstName());  // Added

        // Test with email
        Optional<UserModel> byEmail = userRepository.findByUsernameOrEmail("test@example.com");
        assertTrue(byEmail.isPresent());
        assertEquals("Test", byEmail.get().getFirstName());  // Added

        // Test case insensitivity
        Optional<UserModel> byUpperCase = userRepository.findByUsernameOrEmail("TESTUSER");
        assertTrue(byUpperCase.isPresent());
        assertEquals("Test", byUpperCase.get().getFirstName());  // Added
    }

    @Test
    void whenFindByUsernameOrEmailWithAuthorities_thenReturnUserWithAuthorities() {
        Optional<UserModel> foundUser = userRepository.findByUsernameOrEmailWithAuthorities("testuser");
        assertTrue(foundUser.isPresent());
        assertFalse(foundUser.get().getAuthorities().isEmpty());
        assertEquals("Test", foundUser.get().getFirstName());  // Added
    }

    @Test
    void whenFindByNonExistentUsernameOrEmail_thenReturnEmpty() {
        Optional<UserModel> foundUser = userRepository.findByUsernameOrEmail("nonexistent");
        assertFalse(foundUser.isPresent());
    }

    // Additional edge cases
    @Test
    void whenFindByNullUsername_thenReturnEmpty() {
        Optional<UserModel> foundUser = userRepository.findByUsername(null);
        assertFalse(foundUser.isPresent());
    }

    @Test
    void whenExistsByNullUsername_thenReturnFalse() {
        assertFalse(userRepository.existsByUsername(null));
    }

    @Test
    void whenFindByEmptyUsername_thenReturnEmpty() {
        Optional<UserModel> foundUser = userRepository.findByUsername("");
        assertFalse(foundUser.isPresent());
    }

    @Test
    void whenSaveUserWithoutFirstNameOrLastName_thenThrowException() {
        // Missing firstName
        UserModel noFirstName = UserModel.builder()
                .username("nofirst")
                .password("password")
                .email("nofirst@example.com")
                .lastName("User")  // Only lastName
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(noFirstName);
        });

        // Missing lastName
        UserModel noLastName = UserModel.builder()
                .username("nolast")
                .password("password")
                .email("nolast@example.com")
                .firstName("No")  // Only firstName
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(noLastName);
        });
    }
}