package ru.practicum.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.category.NewCategoryDto;
import ru.practicum.exception.CategoryNotFoundException;
import ru.practicum.exception.CategoryVoidViolationException;
import ru.practicum.exception.NameUniquenessViolationException;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
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
                .orElseThrow(() -> new CategoryNotFoundException(
                        String.format("Category with id=%d was not found", catId))));
    }

    @Override
    public CategoryDto save(NewCategoryDto newCategoryDto) {
        if (categoryRepository.findByName(newCategoryDto.getName()) != null) {
            throw new NameUniquenessViolationException(
                    String.format("Name %s already exists", newCategoryDto.getName()));
        }
        Category category = CategoryMapper.toCategory(newCategoryDto);
        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Override
    public void delete(long catId) {
        if (categoryRepository.existsById(catId)) {
            Category category = categoryRepository.findById(catId).get();
            List<Event> events = eventRepository.findByCategory(category);
            if (events.isEmpty()) {
                categoryRepository.deleteById(catId);
            } else {
                throw new CategoryVoidViolationException("The category is not empty");
            }
        }
    }

    @Override
    public CategoryDto update(long catId, CategoryDto categoryDto) {
        if (categoryRepository.existsById(catId)) {
            Category updatedCategory = categoryRepository.findById(catId).get();
            if (categoryRepository.findByIdNotAndName(catId, categoryDto.getName()) != null) {
                throw new NameUniquenessViolationException(
                        String.format("Name %s already exists", categoryDto.getName()));
            } else {
                updatedCategory.setName(categoryDto.getName());
            }
            return CategoryMapper.toCategoryDto(categoryRepository.save(updatedCategory));
        } else {
            throw new CategoryNotFoundException(String.format("Category with id=%d was not found", catId));
        }
    }
}
