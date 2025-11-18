package ru.practicum.categories.service;

import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;

import java.util.List;

@Service
public interface CategoryService {
    List<CategoryDto> getCategories(Integer from, Integer size);

    CategoryDto addCategory(@Valid NewCategoryDto request);

    void deleteCategory(Long catId);

    CategoryDto updateCategoryName(Long catId, String name);

    CategoryDto getCategoryById(Long catId);
}
