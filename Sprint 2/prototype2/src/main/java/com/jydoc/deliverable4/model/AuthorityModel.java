package com.jydoc.deliverable4.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "authorities")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class AuthorityModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String authority;

    @ManyToMany(mappedBy = "authorities")
    @Builder.Default
    private Set<UserModel> users = new HashSet<>();

    // Added constructor
    public AuthorityModel(String authority) {
        this.authority = authority;
    }

    // Custom builder
    public static class AuthorityModelBuilder {
        private Set<UserModel> users = new HashSet<>();
    }
}