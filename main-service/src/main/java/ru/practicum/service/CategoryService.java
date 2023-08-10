package ru.practicum.service;

import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAll(int from, int size);

    CategoryDto getById(long catId);

    CategoryDto save(NewCategoryDto newCategoryDto);

    void delete(long catId);

    CategoryDto update(long catId, CategoryDto categoryDto);
}
