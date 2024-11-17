package org.example.github2.Form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class LoginForm {
    @Email
    @NotEmpty
    private String email;
    @NotEmpty(message = "не может быть пустым")
    private String password;
}
