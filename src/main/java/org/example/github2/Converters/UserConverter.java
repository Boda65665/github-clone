package org.example.github2.Converters;

import org.example.github2.DTOs.UserDTO;
import org.example.github2.Entity.User;

public class UserConverter {

    public UserDTO userToUserDTO(User user){
        UserDTO convertedUser = new UserDTO();
        convertedUser.setId(user.getId());
        convertedUser.setPassword(user.getPassword());
        convertedUser.setEmail(user.getEmail());
        convertedUser.setLogin(user.getLogin());
        convertedUser.setRepositories(user.getRepositories());
        convertedUser.setRole(user.getRole());
        return convertedUser;
    }
}
