package com.jydoc.deliverable4.repositories;

import com.jydoc.deliverable4.model.AuthorityModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<AuthorityModel, Long> {

    // Return full AuthorityModel objects instead of just authority strings
    @Query("SELECT a FROM AuthorityModel a JOIN a.users u WHERE u.id = :userId")
    List<AuthorityModel> findAuthoritiesByUserId(@Param("userId") Long userId);

    // Keep this as is since it returns full entities
    Optional<AuthorityModel> findByAuthority(String authority);
}