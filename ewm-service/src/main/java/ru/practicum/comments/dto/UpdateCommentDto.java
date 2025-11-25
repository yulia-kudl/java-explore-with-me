package ru.practicum.comments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @NotNull
    private Long id;
    @NotBlank(message = "must not be blank")
    @Size(min = 1, max = 1000, message = "name length must be between 1 and 1000 characters")
    private String text;

}
