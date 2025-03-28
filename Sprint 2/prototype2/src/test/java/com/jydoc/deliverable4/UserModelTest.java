package com.jydoc.deliverable4;

import com.jydoc.deliverable4.model.UserModel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserModelTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private PlatformTransactionManager transactionManager;

    // Helper method for transactional operations
    private void executeInTransaction(Runnable operation) {
        new TransactionTemplate(transactionManager).execute(status -> {
            operation.run();
            return null;
        });
    }

    @Test
    @Transactional
    void testUserEntityPersistence() {
        // Given
        UserModel user = UserModel.builder()
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .firstName("firstname")
                .lastName("lastname")
                .build();

        // When
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        UserModel found = entityManager.find(UserModel.class, user.getId());
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("encodedPassword", found.getPassword());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    void testUsernameUniquenessConstraint() {
        // First user (should succeed)
        executeInTransaction(() -> {
            UserModel user1 = UserModel.builder()
                    .username("uniqueuser")
                    .password("password1")
                    .firstName("firstname")
                    .lastName("lastname")
                    .build();
            entityManager.persist(user1);
        });

        // Second user with same username (should fail)
        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            executeInTransaction(() -> {
                UserModel user2 = UserModel.builder()
                        .username("uniqueuser")
                        .password("password2")
                        .build();
                entityManager.persist(user2);
                entityManager.flush(); // Explicit flush to force immediate constraint check
            });
        });
    }

    @Test
    void testEmailUniquenessConstraint() {
        // First user with email (should succeed)
        executeInTransaction(() -> {
            UserModel user1 = UserModel.builder()
                    .username("user1")
                    .firstName("firstname")
                    .lastName("lastname")
                    .password("password1")
                    .email("unique@example.com")
                    .build();
            entityManager.persist(user1);
        });

        // Second user with same email (should fail)
        assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
            executeInTransaction(() -> {
                UserModel user2 = UserModel.builder()
                        .username("user2")
                        .password("password2")
                        .firstName("firstname")
                        .lastName("lastname")
                        .email("unique@example.com")
                        .build();
                entityManager.persist(user2);
            });
        });
    }

    @Test
    @Transactional
    void testOptionalEmailField() {
        // Given
        UserModel user = UserModel.builder()
                .username("noemail")
                .password("password")
                .firstName("firstname")
                .lastName("lastname")
                .email(null)
                .build();

        // When
        entityManager.persist(user);
        entityManager.flush();
        entityManager.clear();

        // Then
        UserModel found = entityManager.find(UserModel.class, user.getId());
        assertNull(found.getEmail());
    }

    @Test
    @Transactional
    void testDefaultSecurityFlags() {
        UserModel user = UserModel.builder()
                .username("defaultuser")
                .password("password")
                .build();

        assertAll(
                () -> assertTrue(user.isEnabled()),
                () -> assertTrue(user.isAccountNonExpired()),
                () -> assertTrue(user.isCredentialsNonExpired()),
                () -> assertTrue(user.isAccountNonLocked())
        );
    }

    @Test
    @Transactional
    void testFieldLengthConstraints() {
        // Test username max length (50)
        assertThrows(Exception.class, () -> {
            UserModel user = UserModel.builder()
                    .username("a".repeat(51))
                    .password("password")
                    .build();
            entityManager.persist(user);
            entityManager.flush();
        });

        // Test password max length (100)
        assertThrows(Exception.class, () -> {
            UserModel user = UserModel.builder()
                    .username("lengthuser")
                    .password("a".repeat(101))
                    .build();
            entityManager.persist(user);
            entityManager.flush();
        });

        // Test email max length (100)
        assertThrows(Exception.class, () -> {
            UserModel user = UserModel.builder()
                    .username("emailuser")
                    .password("password")
                    .email("a".repeat(90) + "@example.com") // > 100 chars
                    .build();
            entityManager.persist(user);
            entityManager.flush();
        });
    }

    @Test
    void testBuilderPattern() {
        // Given
        UserModel user = UserModel.builder()
                .username("builderuser")
                .password("password")
                .email("builder@example.com")
                .enabled(false)
                .accountNonExpired(false)
                .credentialsNonExpired(false)
                .accountNonLocked(false)
                .build();

        // Then
        assertAll(
                () -> assertEquals("builderuser", user.getUsername()),
                () -> assertEquals("password", user.getPassword()),
                () -> assertEquals("builder@example.com", user.getEmail()),
                () -> assertFalse(user.isEnabled()),
                () -> assertFalse(user.isAccountNonExpired()),
                () -> assertFalse(user.isCredentialsNonExpired()),
                () -> assertFalse(user.isAccountNonLocked())
        );
    }

    @Test
    void testToBuilder() {
        // Given
        UserModel original = UserModel.builder()
                .username("original")
                .password("password")
                .build();

        // When
        UserModel modified = original.toBuilder()
                .username("modified")
                .enabled(false)
                .build();

        // Then
        assertAll(
                () -> assertEquals("original", original.getUsername()),
                () -> assertTrue(original.isEnabled()),
                () -> assertEquals("modified", modified.getUsername()),
                () -> assertFalse(modified.isEnabled())
        );
    }
}