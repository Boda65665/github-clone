package org.example.github2.Form;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateRepositoryForm {
    @NonNull
    private String name;
    @NotNull
    private Boolean isPrivate;
}
