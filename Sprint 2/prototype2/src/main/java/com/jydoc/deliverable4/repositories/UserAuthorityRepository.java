package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.UserAuthority;
import com.jydoc.deliverable4.model.UserAuthorityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing {@link UserAuthority} entities.
 * Provides methods to query user-authority relationships from the database.
 */
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, UserAuthorityId> {

    /**
     * Finds all user-authority relationships for a given user ID using JPQL.
     *
     * @param userId the ID of the user to search for
     * @return list of user-authority relationships
     */
    @Query("SELECT ua FROM UserAuthority ua WHERE ua.userId = :userId")
    List<UserAuthority> findAllByUserId(@Param("userId") Long userId);

    /**
     * Finds all user-authority relationships for a given user ID using native SQL.
     *
     * @param userId the ID of the user to search for
     * @return list of user-authority relationships
     */
    @Query(value = "SELECT * FROM user_authorities WHERE user_id = :userId", nativeQuery = true)
    List<UserAuthority> findAllByUserIdNative(@Param("userId") Long userId);
}