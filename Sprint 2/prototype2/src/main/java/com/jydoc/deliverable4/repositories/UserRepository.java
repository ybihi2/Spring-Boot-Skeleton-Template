package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Long> {

    // Standard username lookup
    Optional<UserModel> findByUsername(String username);

    // Username lookup with authorities eager loading
    @Query("SELECT DISTINCT u FROM UserModel u LEFT JOIN FETCH u.authorities WHERE u.username = :username")
    Optional<UserModel> findByUsernameWithAuthorities(@Param("username") String username);

    // Existence checks
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // Combined username/email lookup (basic)
    @Query("SELECT u FROM UserModel u WHERE LOWER(u.username) = LOWER(:credential) OR LOWER(u.email) = LOWER(:credential)")
    Optional<UserModel> findByUsernameOrEmail(@Param("credential") String credential);




    // Combined username/email lookup with authorities eager loading
    @Query("SELECT DISTINCT u FROM UserModel u LEFT JOIN FETCH u.authorities " +
            "WHERE u.username = :credential OR u.email = :credential")
    Optional<UserModel> findByUsernameOrEmailWithAuthorities(@Param("credential") String credential);
}