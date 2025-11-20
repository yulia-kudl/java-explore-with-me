package ru.practicum.categories.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.ErrorHandler.CategoryNotEmptyException;
import ru.practicum.ErrorHandler.EntityNotFoundException;
import ru.practicum.categories.dto.CategoryDto;
import ru.practicum.categories.dto.NewCategoryDto;
import ru.practicum.categories.entity.CategoryEntity;
import ru.practicum.categories.repository.CategoryRepository;
import ru.practicum.events.repository.EventsRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryEntityMapper mapper;
    private final EventsRepository eventsRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(NewCategoryDto request) {
        return mapper.toDTO(categoryRepository.save(mapper.toEntity(request)));
    }

    @Override
    public void deleteCategory(Long catId) {
        if (categoryRepository.findById(catId).isEmpty()) {
            throw new EntityNotFoundException(catId, "Category");
        }
        if (eventsRepository.existsByCategoryId(catId)) {
            throw new CategoryNotEmptyException("The category is not empty");
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto updateCategoryName(Long catId, String name) {
        CategoryEntity category = categoryRepository.findById(catId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setName(name);
        return mapper.toDTO(categoryRepository.save(category));
    }

    @Override
    public List<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id").ascending());
        return categoryRepository.findAll(pageable)
                .stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        // 404 - нет категории
        CategoryEntity entity = categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(catId, "Category"));

        return mapper.toDTO(entity);
    }
}
