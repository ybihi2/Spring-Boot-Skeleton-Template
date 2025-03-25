//This is where user data is processed.



package com.jydoc.deliverable3.Service;
import com.jydoc.deliverable3.DTO.UserDTO;
import com.jydoc.deliverable3.Model.UserModel;

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
}
