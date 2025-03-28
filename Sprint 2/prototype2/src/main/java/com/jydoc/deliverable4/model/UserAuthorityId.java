package com.jydoc.deliverable4.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key class for {@link UserAuthority} entity.
 * <p>
 * Represents the compound key consisting of user ID and authority ID
 * used in the user-authority many-to-many relationship mapping.
 *
 * @see UserAuthority
 */
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorityId implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The ID of the user in the relationship.
     * Part of the composite primary key.
     */
    private Long userId;

    /**
     * The ID of the authority in the relationship.
     * Part of the composite primary key.
     */
    private Long authorityId;

    /**
     * Compares this composite key with another object for equality.
     * @param o the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuthorityId that = (UserAuthorityId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(authorityId, that.authorityId);
    }

    /**
     * Generates a hash code for this composite key.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(userId, authorityId);
    }
}