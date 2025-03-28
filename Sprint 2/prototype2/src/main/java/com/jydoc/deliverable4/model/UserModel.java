package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(unique = true, length = 100)
    private String email;

    @Builder.Default
    private boolean enabled = true;

    @Builder.Default
    private boolean accountNonExpired = true;

    @Builder.Default
    private boolean credentialsNonExpired = true;

    @Builder.Default
    private boolean accountNonLocked = true;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_authorities",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "authority_id")
    )
    @Fetch(FetchMode.JOIN)
    @Builder.Default
    private Set<AuthorityModel> authorities = new HashSet<>();

    // Authority management methods
    public void addAuthority(AuthorityModel authority) {
        this.authorities.add(authority);
        authority.getUsers().add(this);
    }

    public void removeAuthority(AuthorityModel authority) {
        this.authorities.remove(authority);
        authority.getUsers().remove(this);
    }

    // Custom builder to handle default values
    public static class UserModelBuilder {
        private boolean enabled = true;
        private boolean accountNonExpired = true;
        private boolean credentialsNonExpired = true;
        private boolean accountNonLocked = true;
        private Set<AuthorityModel> authorities = new HashSet<>();
    }
}