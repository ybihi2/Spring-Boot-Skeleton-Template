package com.jydoc.deliverable4.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_authorities")
@IdClass(UserAuthorityId.class)
public class UserAuthority {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "authority_id")
    private Long authorityId;

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getAuthorityId() { return authorityId; }
    public void setAuthorityId(Long authorityId) { this.authorityId = authorityId; }
}