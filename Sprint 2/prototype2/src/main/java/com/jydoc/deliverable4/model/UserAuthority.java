package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Represents the many-to-many relationship between users and authorities.
 * This entity serves as a join table that maps users to their assigned authorities/roles.
 * <p>
 * Uses a composite primary key represented by {@link UserAuthorityId} consisting of
 * {@code userId} and {@code authorityId}.
 *
 * @see UserAuthorityId
 */
@Entity
@Getter
@Setter
@Table(name = "user_authorities")
@IdClass(UserAuthorityId.class)
public class UserAuthority {

    /**
     * The ID of the user associated with this authority mapping.
     * Part of the composite primary key.
     * Cannot be null.
     */
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * The ID of the authority associated with this user mapping.
     * Part of the composite primary key.
     * Cannot be null.
     */
    @Id
    @Column(name = "authority_id", nullable = false)
    private Long authorityId;
}