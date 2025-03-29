package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user entity in the system with authentication and authorization details.
 * This class maps to the 'users' table in the database and includes user credentials,
 * account status flags, and associated authorities (roles/permissions).
 *
 * <p>The class uses Lombok annotations for boilerplate code reduction and JPA annotations
 * for ORM mapping. It maintains a many-to-many relationship with AuthorityModel.</p>
 *
 * @author Your Name
 * @version 1.0
 * @see AuthorityModel
 * @since 1.0
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserModel {

    /**
     * Unique identifier for the user. Automatically generated by the database.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for authentication. Must be non-null and maximum 50 characters.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Password for authentication. Stored in encrypted form. Must be non-null and maximum 100 characters.
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * Unique email address for the user. Maximum 100 characters.
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * Flag indicating whether the user is enabled. Defaults to true.
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Flag indicating whether the user's account is non-expired. Defaults to true.
     */
    @Builder.Default
    private boolean accountNonExpired = true;

    /**
     * Flag indicating whether the user's credentials are non-expired. Defaults to true.
     */
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Flag indicating whether the user's account is non-locked. Defaults to true.
     */
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * Set of authorities (roles/permissions) assigned to the user.
     * Maintains a many-to-many relationship through the 'user_authorities' join table.
     * Uses eager fetching strategy and cascades persist and merge operations.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_authorities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    @Fetch(FetchMode.JOIN)
    @Builder.Default
    private Set<AuthorityModel> authorities = new HashSet<>();

    /**
     * Returns an unmodifiable defensive copy of the authorities set.
     * This protects the internal representation while maintaining immutability guarantees.
     *
     * @return unmodifiable set containing a copy of the authorities
     */
    public Set<AuthorityModel> getAuthorities() {
        return Collections.unmodifiableSet(new HashSet<>(this.authorities));
    }

    /**
     * Safely replaces the authorities collection with a copy of the input.
     * Null input results in an empty set.
     *
     * @param authorities collection of authorities to set
     */
    public void setAuthorities(Collection<AuthorityModel> authorities) {
        this.authorities = authorities != null
                ? new HashSet<>(authorities)
                : new HashSet<>();
    }





    /**
     * Adds an authority to the user and maintains the bidirectional relationship.
     *
     * @param authority the authority to add
     * @throws NullPointerException if the authority parameter is null
     */
    public void addAuthority(AuthorityModel authority) {
        this.authorities.add(authority);
        authority.getUsers().add(this);
    }

    /**
     * Removes an authority from the user and maintains the bidirectional relationship.
     *
     * @param authority the authority to remove
     */
    public void removeAuthority(AuthorityModel authority) {
        this.authorities.remove(authority);
        authority.getUsers().remove(this);
    }

    /**
     * Custom builder class for UserModel with default values.
     */
    public static class UserModelBuilder {
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonLocked = true;
        private Set<AuthorityModel> authorities = new HashSet<>();
    }
}