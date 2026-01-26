package ru.practicum.ewm.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.ewm.dal.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Boolean existsByName(String name);

    Boolean existsByNameAndIdNot(String name, Long id);
}
