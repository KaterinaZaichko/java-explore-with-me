package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.CategoryVoidViolationException;
import ru.practicum.exception.EntityNotFoundException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CategoryDto> getAll(int from, int size) {
        List<CategoryDto> categories = new ArrayList<>();
        Pageable pageWithSomeElements = PageRequest.of(from > 0 ? from / size : 0, size);
        for (Category category : categoryRepository.findAll(pageWithSomeElements)) {
            categories.add(CategoryMapper.toCategoryDto(category));
        }
        return categories;
    }

    @Override
    public CategoryDto getById(long catId) {
        return CategoryMapper.toCategoryDto(categoryRepository.findById(catId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Category with id=%d was not found", catId))));
    }

    @Override
    public CategoryDto save(NewCategoryDto newCategoryDto) {
        return CategoryMapper.toCategoryDto(categoryRepository.save(CategoryMapper.toCategory(newCategoryDto)));
    }

    @Override
    public void delete(long catId) {
        if (categoryRepository.existsById(catId)) {
            if (!eventRepository.existsByCategory(catId)) {
                categoryRepository.deleteById(catId);
            } else {
                throw new CategoryVoidViolationException("The category is not empty");
            }
        }
    }

    @Override
    public CategoryDto update(long catId, CategoryDto categoryDto) {
        Category updatedCategory = categoryRepository.findById(catId).orElseThrow(() -> new EntityNotFoundException(
                String.format("Category with id=%d was not found", catId)));
        updatedCategory.setName(categoryDto.getName());
        return CategoryMapper.toCategoryDto(categoryRepository.save(updatedCategory));
    }
}
