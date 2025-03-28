package com.jydoc.deliverable4.model;

import java.io.Serializable;
import java.util.Objects;

public class UserAuthorityId implements Serializable {
    private Long userId;
    private Long authorityId;

    public UserAuthorityId() {}

    public UserAuthorityId(Long userId, Long authorityId) {
        this.userId = userId;
        this.authorityId = authorityId;
    }

    // equals() and hashCode()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAuthorityId that = (UserAuthorityId) o;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(authorityId, that.authorityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, authorityId);
    }
}