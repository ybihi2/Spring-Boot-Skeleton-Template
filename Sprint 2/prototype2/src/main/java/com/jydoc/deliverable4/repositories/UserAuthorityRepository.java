package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.UserAuthority;
import com.jydoc.deliverable4.model.UserAuthorityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserAuthorityRepository extends JpaRepository<UserAuthority, UserAuthorityId> {

    // Fixed parameter name consistency
    @Query("SELECT ua FROM UserAuthority ua WHERE ua.userId = :userId")
    List<UserAuthority> findByUserId(@Param("userId") Long userId);

    // Fixed native query with consistent parameter naming
    @Query(value = "SELECT * FROM user_authorities WHERE user_id = :userId", nativeQuery = true)
    List<UserAuthority> findUserAuthoritiesByUserId(@Param("userId") Long userId);


}