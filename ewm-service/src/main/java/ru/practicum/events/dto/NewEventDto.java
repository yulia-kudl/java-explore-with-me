package ru.practicum.events.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewEventDto {
    @NotBlank(message = "must not be blank")
    @Size(min = 20, max = 2000, message = "annotation length must be between 1 and 50 characters")
    private String annotation;
    @NotNull
    private Long category;
    @NotBlank(message = "must not be blank")
    @Size(min = 20, max = 7000, message = "description length must be between 1 and 50 characters")
    private String description;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Future
    private LocalDateTime eventDate;
    @NotNull
    private Location location;
    private Boolean paid;
    @PositiveOrZero
    private Integer participantLimit;
    private Boolean requestModeration;
    @NotBlank(message = "must not be blank")
    @Size(min = 3, max = 120, message = "title length must be between 3 and 120 characters")
    private String title;


   /* @AssertTrue(message = "Field: eventDate. Error: должно содержать дату, которая еще не наступила. Value")
    public boolean isEventDateValid() {
        if (eventDate == null)
            return false;

        try {
            LocalDateTime date = LocalDateTime.parse(eventDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return date.isAfter(LocalDateTime.now().plusHours(2));
        } catch (Exception e) {
            return false;
        }
    } */
}

