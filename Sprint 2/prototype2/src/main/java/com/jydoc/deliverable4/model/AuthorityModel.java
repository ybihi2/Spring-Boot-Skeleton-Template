package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an authority/role in the system that can be assigned to users.
 * Authorities define permissions or access levels for users.
 */
@Entity
@Table(name = "authorities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthorityModel {

    /**
     * Unique identifier for the authority.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the authority (e.g., "ROLE_ADMIN", "ROLE_USER").
     * Must be unique and cannot be null.
     */
    @Column(nullable = false, unique = true, length = 50)
    private String authority;

    /**
     * Set of users who have been granted this authority.
     * Represents the many-to-many relationship with UserModel.
     */
    @ManyToMany(mappedBy = "authorities")
    @Builder.Default
    private Set<UserModel> users = new HashSet<>();

    /**
     * Constructs an AuthorityModel with the given authority name.
     *
     * @param authority the name of the authority
     */
    public AuthorityModel(String authority) {
        this.authority = authority;
    }

    /**
     * Custom builder class for AuthorityModel that initializes the users set.
     */
    public static class AuthorityModelBuilder {
        /**
         * The set of users for this authority, initialized as empty HashSet.
         */
        private Set<UserModel> users = new HashSet<>();
    }
}