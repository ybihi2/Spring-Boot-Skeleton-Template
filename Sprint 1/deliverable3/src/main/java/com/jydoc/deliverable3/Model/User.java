package com.jydoc.deliverable3.Model;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user-id")
    private Long id;

    @Column(name="user-firstname")
    private String firstName;

    @Column(name="user-lastname")
    private String lastName;

    @Column(name="user-email")
    private String email;

    @Column(name="user-password")
    private String password;

}
