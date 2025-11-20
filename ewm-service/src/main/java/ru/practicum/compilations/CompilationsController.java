package ru.practicum.compilations;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.service.CompilationService;

import java.util.List;

@RestController
@RequestMapping(path = "/")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CompilationsController {
    private final CompilationService compilationService;

    //public
    // GET /compilations?pinned={pinned}&from={from]&size={size}
    @GetMapping("compilations")
    List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "0") Integer from
    ) {
        return compilationService.getCompilations(pinned, size, from);
    }

    // GET compilations/{compId}
    @GetMapping("compilations/{compId}")
    CompilationDto getCompilation(@PathVariable Long compId) {
        return compilationService.getCompilationById(compId);
    }

    //admin

    // POST /admin/compilations
    @PostMapping("admin/compilations")
    @ResponseStatus(HttpStatus.CREATED)
    CompilationDto addCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto) {
        return compilationService.addCompilation(newCompilationDto);
    }

    // DELETE /admin/compilations/{compId}
    @DeleteMapping("admin/compilations/{compId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }


    // PATCH /admin/compilations/{compId}
    @PatchMapping("admin/compilations/{compId}")
    CompilationDto updateCompilation(@PathVariable Long compId, @RequestBody @Valid UpdateCompilationRequest update) {
        return compilationService.updateCompilation(compId, update);
    }


}
