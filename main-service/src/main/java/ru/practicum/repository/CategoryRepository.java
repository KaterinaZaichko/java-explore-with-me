package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Category;

import java.util.Set;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByIdNotAndName(long id, String name);

    Category findByName(String name);

    Set<Category> findByIdIn(long[] categories);
}
