package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for {@link UserModel} entities providing user lookup operations.
 * Includes methods for finding users with different loading strategies for authorities.
 */
public interface UserRepository extends JpaRepository<UserModel, Long> {

    // Basic user lookups
    Optional<UserModel> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    /**
     * Finds a user by username with authorities eagerly loaded.
     * @param username the username to search for
     * @return user with authorities loaded
     */
    @Query("SELECT DISTINCT u FROM UserModel u LEFT JOIN FETCH u.authorities WHERE u.username = :username")
    Optional<UserModel> findByUsernameWithAuthorities(@Param("username") String username);

    /**
     * Finds a user by username or email (case-insensitive).
     * @param credential username or email to search for
     * @return matching user if found
     */
    @Query("SELECT u FROM UserModel u WHERE LOWER(u.username) = LOWER(:credential) OR LOWER(u.email) = LOWER(:credential)")
    Optional<UserModel> findByUsernameOrEmail(@Param("credential") String credential);

    /**
     * Finds a user by username or email with authorities eagerly loaded.
     * @param credential username or email to search for
     * @return matching user with authorities if found
     */
    @Query("SELECT DISTINCT u FROM UserModel u LEFT JOIN FETCH u.authorities " +
            "WHERE u.username = :credential OR u.email = :credential")
    Optional<UserModel> findByUsernameOrEmailWithAuthorities(@Param("credential") String credential);
}