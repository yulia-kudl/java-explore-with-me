package ru.practicum.compilations.service;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.entity.CategoryEntity;
import ru.practicum.compilations.dto.CompilationDto;
import ru.practicum.compilations.dto.NewCompilationDto;
import ru.practicum.compilations.dto.UpdateCompilationRequest;
import ru.practicum.compilations.entity.CompilationEntity;
import ru.practicum.events.entity.EventEntity;
import ru.practicum.events.service.EventEntityMapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {EventEntityMapper.class}) //для маппинга эвентов
public interface CompilationEntityMapper {

    // анализируем в ручную в сервисе
    @Mapping(target = "events", ignore = true)
    CompilationEntity toEntity(NewCompilationDto requestDTO);

    // анализируем в ручную в сервис
 //   @Mapping(target = "events", ignore = true)
  //  CompilationEntity toEntity(UpdateCompilationRequest updateDto);

    CompilationDto toDto(CompilationEntity entity);


}
