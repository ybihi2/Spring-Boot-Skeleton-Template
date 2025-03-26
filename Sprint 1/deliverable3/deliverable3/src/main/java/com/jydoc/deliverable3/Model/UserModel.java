//The User Model is used to interact and create the database


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

public class UserModel {
//Each row in the database is a unique user containing all of these columns
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="userid")
    private int id;

    @Column(name="userisadmin")
    private boolean admin;

    @Column(name="userfirstname")
    private String firstName;

    @Column(name="userlastname")
    private String lastName;

    @Column(name="useremail")
    private String email;

    @Column(name="userpassword")
    private String password;

}
