package ru.practicum.users.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequest {
    @NotBlank
    @Size(min = 2, max = 250, message = "name length must be between 2 and 250 characters")
    private String name;
    @NotNull
    @Email
    @Size(min = 6, max = 254, message = "email length must be between 6 and 254 characters")
    private String email;
}
