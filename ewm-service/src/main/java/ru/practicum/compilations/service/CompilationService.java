package ru.practicum.compilations.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;

import java.util.List;

@Service
public interface CompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer size, Integer from);

    CompilationDto getCompilationById(Long compId);

    CompilationDto addCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest update);
}
