package org.example.github2.Form;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class RegisterForm {
    @Email
    @NotEmpty(message = "не может быть пустым")
    private String email;
    @Size(min = 8,max = 20,message = "size password must be from 8 to 20 characters")
    private String password;
    @Size(min = 3,max = 20,message = "min 3 max 20")
    private String login;
}
