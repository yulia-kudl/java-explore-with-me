package ru.practicum.categories.service;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.entity.CategoryEntity;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoryEntityMapper {
    CategoryEntity toEntity(CategoryDto requestDTO);

    CategoryEntity toEntity(NewCategoryDto requestDTO);

    CategoryDto toDTO(CategoryEntity entity);
}