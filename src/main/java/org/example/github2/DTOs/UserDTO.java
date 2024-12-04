package org.example.github2.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.github2.Entity.Repository;
import org.example.github2.Model.Role;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private int id;
    private String email;
    private String password;
    private String login;
    private Role role;
    private List<Repository> repositories;

}
