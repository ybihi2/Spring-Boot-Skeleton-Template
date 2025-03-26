//This is where user data is processed.

package com.jydoc.deliverable3.Service;
import com.jydoc.deliverable3.DTO.UserDTO;
import com.jydoc.deliverable3.Model.UserModel;
import com.jydoc.deliverable3.Repository.UserRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserService {


    public UserModel convertToEntity(UserDTO UserDto) {

        UserModel user = new UserModel();
        user.setId(UserDto.getId());
        user.setAdmin(UserDto.isAdmin());
        user.setFirstName(UserDto.getFirstName());
        user.setLastName(UserDto.getLastName());
        user.setEmail(UserDto.getEmail());
        user.setPassword(UserDto.getPassword());
        return user;
    }

    public UserDTO convertToDTO(UserModel UserModel) {

        UserDTO user = new UserDTO();
        user.setId(UserModel.getId());
        user.setAdmin(UserModel.isAdmin());
        user.setFirstName(UserModel.getFirstName());
        user.setLastName(UserModel.getLastName());
        user.setEmail(UserModel.getEmail());
        user.setPassword(UserModel.getPassword());
        return user;
    }





    public boolean authenticate(@NotBlank(message = "Email cannot be empty") @Email(message = "Invalid email format") String email, @NotBlank(message = "Password cannot be empty") @Size(min = 6, message = "Password must be at least 6 characters") @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).{6,}$",
             message = "Password must contain at least one letter and one number") String password) {
        return true;
    }
}
