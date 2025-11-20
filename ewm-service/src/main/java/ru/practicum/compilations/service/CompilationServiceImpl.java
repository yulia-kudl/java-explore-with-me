package ru.practicum.compilations.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ErrorHandler.EntityNotFoundException;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.entity.CompilationEntity;
import ru.practicum.compilations.repository.CompilationRepository;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.repository.EventsRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@AllArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final CompilationEntityMapper mapper;
    private final EventsRepository eventsRepository;

    @Override
    public List<CompilationDto> getCompilations(Boolean pinned, Integer size, Integer from) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        Page<CompilationEntity> result = pinned == null ? compilationRepository.findAll(pageable) :
                compilationRepository.findAllByPinned(pinned, pageable);
        return result
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        // 404 подборка не найдена
        CompilationEntity entity = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(compId, "Compilation"));

        return mapper.toDto(entity);
    }

    @Override
    public CompilationDto addCompilation(NewCompilationDto newCompilationDto) {
        CompilationEntity newEntity = mapper.toEntity(newCompilationDto);

        Set<EventEntity> events = new HashSet<>();
        if (newCompilationDto.getEvents() != null && !newCompilationDto.getEvents().isEmpty()) {
            List<EventEntity> loadedEvents = eventsRepository.findAllById(newCompilationDto.getEvents());

            //  все события существуют?
            if (loadedEvents.size() != newCompilationDto.getEvents().size()) {
                throw new EntityNotFoundException(null, "One or more events not found in list");
            }
            events = new HashSet<>(loadedEvents);
        }
        newEntity.setEvents(events);

        return mapper.toDto(compilationRepository.save(newEntity));
    }

    @Override
    @Transactional
    public void deleteCompilation(Long compId) {
        // 404 NOT FOUND
        CompilationEntity entity = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(compId, "Compilation"));
        compilationRepository.delete(entity);
    }

    @Override
    public CompilationDto updateCompilation(Long compId, UpdateCompilationRequest update) {
        CompilationEntity entity = compilationRepository.findById(compId)
                .orElseThrow(() -> new EntityNotFoundException(compId, "Compilation"));
        if (update.getPinned() != null) {
            entity.setPinned(update.getPinned());
        }
        if (update.getTitle() != null) {
            entity.setTitle(update.getTitle());
        }

        if (update.getEvents() != null) {
            List<EventEntity> loadedEvents = eventsRepository.findAllById(update.getEvents());
            //  все события существуют?
            if (loadedEvents.size() != update.getEvents().size()) {
                throw new EntityNotFoundException(null, "One or more events not found in list");
            }
            Set<EventEntity> events = new HashSet<>(loadedEvents);
            entity.getEvents().clear();
            entity.setEvents(events);

        }

        return mapper.toDto(compilationRepository.save(entity));
    }
}
