package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.auth.AuthorityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing {@link AuthorityModel} entities.
 * Provides methods to query authority data from the database.
 */
public interface AuthorityRepository extends JpaRepository<AuthorityModel, Long> {

    /**
     * Finds all authority models associated with a specific user ID.
     *
     * @param userId the ID of the user to search for
     * @return a list of authority models associated with the user
     */
    @Query("SELECT a FROM AuthorityModel a JOIN a.users u WHERE u.id = :userId")
    List<AuthorityModel> findAllByUserId(@Param("userId") Long userId);

    /**
     * Finds an authority model by its authority string.
     *
     * @param authority the authority string to search for
     * @return an Optional containing the authority model if found
     */
    Optional<AuthorityModel> findByAuthority(String authority);
}