package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link UserModel} entities providing user lookup operations.
 * Includes methods for finding users with different loading strategies for authorities.
 */
@Repository
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

//    /** TODO: Implement this
//     * Finds the most recent users ordered by creation date.
//     * @param limit maximum number of users to return
//     * @return list of recent users
//     */
//    @Query("SELECT u FROM UserModel u ORDER BY u.createdDate DESC LIMIT :limit")
//    List<UserModel> findTopNByOrderByCreatedDateDesc(@Param("limit") int limit);

    /**
     * Finds all users with their authorities eagerly loaded.
     *
     * @return list of all users with authorities
     */
    @Query("SELECT DISTINCT u FROM UserModel u LEFT JOIN FETCH u.authorities")
    List<UserModel> findAllWithAuthorities();

    /**
     * Checks if a user exists by ID.
     *
     * @param id the user ID to check
     * @return true if user exists, false otherwise
     */
    boolean existsById(Long id);
}