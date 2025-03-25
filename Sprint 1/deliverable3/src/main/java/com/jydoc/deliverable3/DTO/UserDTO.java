// This DTO accepts user input to fill variables, then is converted by
// UserService into a UserModel.
// Also used for business logic.
package com.jydoc.deliverable3.DTO;
import lombok.Data;

@Data public class UserDTO {      //@Data automatically applies all Getters, Setters, NoArgConstructor, and ArgConstructor

    private int id;
    private boolean admin;
    private String email;
    private String password;
    private String firstName;
    private String lastName;

}
