package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user entity in the system, implementing Spring Security's {@link UserDetails}.
 * <p>
 * This entity stores user authentication details and their associated authorities/roles.
 * It includes security-related flags and manages a many-to-many relationship with {@link AuthorityModel}.
 *
 * @see UserDetails
 * @see AuthorityModel
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
     * Unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique username for authentication.
     * Must be non-null and maximum 50 characters.
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * Encrypted password for authentication.
     * Must be non-null and maximum 100 characters.
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * Unique email address for the user.
     * Maximum 100 characters.
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * Indicates whether the user is enabled.
     * Defaults to true.
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * Indicates whether the user's account is expired.
     * Defaults to true.
     */
    @Builder.Default
    private boolean accountNonExpired = true;

    /**
     * Indicates whether the user's credentials are expired.
     * Defaults to true.
     */
    @Builder.Default
    private boolean credentialsNonExpired = true;

    /**
     * Indicates whether the user's account is locked.
     * Defaults to true.
     */
    @Builder.Default
    private boolean accountNonLocked = true;

    /**
     * Set of authorities/roles assigned to the user.
     * Uses eager fetching with JOIN strategy for performance.
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
     * Adds an authority to the user and maintains the bidirectional relationship.
     * @param authority the authority to add
     */
    public void addAuthority(AuthorityModel authority) {
        this.authorities.add(authority);
        authority.getUsers().add(this);
    }

    /**
     * Removes an authority from the user and maintains the bidirectional relationship.
     * @param authority the authority to remove
     */
    public void removeAuthority(AuthorityModel authority) {
        this.authorities.remove(authority);
        authority.getUsers().remove(this);
    }

    /**
     * Custom builder implementation to handle default values properly.
     */
    public static class UserModelBuilder {
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonLocked = true;
        private Set<AuthorityModel> authorities = new HashSet<>();
    }
}