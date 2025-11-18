package ru.practicum.compilations.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.events.dto.EventShortDto;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompilationDto {
    private Set<EventShortDto> events;
    @NotNull
    private Long id;
    @NotNull
    private boolean pinned;
    @NotNull
    private String title;

}
