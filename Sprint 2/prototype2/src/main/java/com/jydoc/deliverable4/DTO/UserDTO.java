package com.jydoc.deliverable4.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;


@Data
@Getter
@Setter
public class UserDTO {

    // Getters and Setters
    @NotEmpty(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    @NotEmpty(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must contain uppercase, lowercase, and number")
    private String password;

    @Email(message = "Invalid email format")
    @NotEmpty(message = "Email is required")
    private String email;

    @NotEmpty(message= "Invalid first name")
    private String firstName;

    @NotEmpty(message= "Invalid last name")
    private String lastName;

    private String authority = "ROLE_USER";


}