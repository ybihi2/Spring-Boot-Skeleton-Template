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
 * Entity class representing a user in the system.
 *
 * <p>This class maps to the "users" database table and includes:</p>
 * <ul>
 *   <li>Core user credentials (username, password)</li>
 *   <li>Personal information (name, email)</li>
 *   <li>Account status flags</li>
 *   <li>Role-based authorities</li>
 * </ul>
 *
 * <p><strong>Security Features:</strong></p>
 * <ul>
 *   <li>Immutable authority collections to prevent unintended modifications</li>
 *   <li>Account status tracking (locked, expired, etc.)</li>
 *   <li>Proper JPA relationship mapping for authorities</li>
 * </ul>
 *
 * @version 1.2
 * @see AuthorityModel The authority/role entity
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
     * Primary key identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for authentication.
     *
     * <p><strong>Constraints:</strong></p>
     * <ul>
     *   <li>Database unique constraint</li>
     *   <li>Non-nullable</li>
     *   <li>Maximum 50 characters</li>
     * </ul>
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Hashed password for authentication.
     *
     * <p><strong>Constraints:</strong></p>
     * <ul>
     *   <li>Non-nullable</li>
     *   <li>Maximum 100 characters (to accommodate hashing)</li>
     * </ul>
     *
     * <p><strong>Security Note:</strong> Should always be stored hashed</p>
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * User's email address.
     *
     * <p><strong>Constraints:</strong></p>
     * <ul>
     *   <li>Database unique constraint</li>
     *   <li>Maximum 100 characters</li>
     * </ul>
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * User's first/given name.
     *
     * <p><strong>Constraints:</strong></p>
     * <ul>
     *   <li>Non-nullable</li>
     *   <li>Maximum 50 characters</li>
     * </ul>
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /**
     * User's last/family name.
     *
     * <p><strong>Constraints:</strong></p>
     * <ul>
     *   <li>Non-nullable</li>
     *   <li>Maximum 50 characters</li>
     * </ul>
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /**
     * Flag indicating if the account is enabled.
     *
     * <p><strong>Default:</strong> true (accounts are enabled by default)</p>
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Flag indicating if the account is non-expired.
     *
     * <p><strong>Default:</strong> true (accounts don't expire by default)</p>
     */
    @Builder.Default
    private boolean accountNonExpired = true;

    /**
     * Flag indicating if credentials are non-expired.
     *
     * <p><strong>Default:</strong> true (credentials don't expire by default)</p>
     */
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Flag indicating if the account is non-locked.
     *
     * <p><strong>Default:</strong> true (accounts aren't locked by default)</p>
     */
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * Set of authorities/roles granted to the user.
     *
     * <p><strong>Relationship Details:</strong></p>
     * <ul>
     *   <li>Many-to-many with AuthorityModel</li>
     *   <li>Eager fetching for immediate availability</li>
     *   <li>Cascade persist and merge operations</li>
     *   <li>Mapped via join table "user_authorities"</li>
     * </ul>
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
     * Returns an unmodifiable view of the user's authorities.
     *
     * @return immutable set of authorities
     */
    public Set<AuthorityModel> getAuthorities() {
        return Collections.unmodifiableSet(new HashSet<>(this.authorities));
    }

    /**
     * Replaces all authorities with the given collection.
     *
     * @param authorities new collection of authorities (null creates empty set)
     */
    public void setAuthorities(Collection<AuthorityModel> authorities) {
        this.authorities = authorities != null ? new HashSet<>(authorities) : new HashSet<>();
    }

    /**
     * Adds a single authority to the user.
     *
     * @param authority the authority to add
     */
    public void addAuthority(AuthorityModel authority) {
        this.authorities.add(authority);
        authority.getUsers().add(this);
    }

    /**
     * Removes a single authority from the user.
     *
     * @param authority the authority to remove
     */
    public void removeAuthority(AuthorityModel authority) {
        this.authorities.remove(authority);
        authority.getUsers().remove(this);
    }
}