package ru.practicum.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.dal.entity.Category;
import ru.practicum.dal.repository.CategoryRepository;
import ru.practicum.dal.repository.EventRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository repository;
    private final CategoryMapper mapper;

    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto create(CategoryDto dto) {
        if (isCategoryNameDuplicate(dto)) {
            throw new ConflictException("Категория уже существует");
        }
        Category category = repository.save(mapper.toEntity(dto));
        log.info("Создана категория {}, id = {}", category.getName(), category.getId());
        return mapper.toDto(category);
    }

    @Transactional
    public CategoryDto update(Long catId, CategoryDto dto) {
        var newDto = mapper.updateDto(getEntity(catId), dto);
        if (isCategoryNameDuplicate(newDto)) {
            throw new ConflictException("Категория уже существует");
        }
        var entity = repository.save(mapper.toEntity(newDto));
        log.info("Изменение категории OK");
        return mapper.toDto(entity);
    }

    @Transactional
    public void delete(Long catId) {
        if (!categoryIsExist(catId)) {
            throw new NotFoundException("Удаляемая запись не найдена");
        }

        if (eventRepository.existsByCategoryId(catId)) {
            throw new ConflictException("Категория с id = " + catId + " используется");
        }
        repository.deleteById(catId);
        log.info("Категория {} удалена", catId);
    }

    @Transactional(readOnly = true)
    public List<CategoryDto> find(int from, int size) {

        int page = from / size;
        var pageable = PageRequest.of(page, size);

        return repository.findAll(pageable)
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Boolean categoryIsExist(Long id) {
        return repository.existsById(id);
    }

    @Transactional(readOnly = true)
    public Boolean isCategoryNameDuplicate(CategoryDto dto) {
        return dto.getId() == null
                ? repository.existsByName(dto.getName())
                : repository.existsByNameAndIdNot(dto.getName(), dto.getId());
    }

    @Transactional(readOnly = true)
    public CategoryDto getEntity(Long userId) {
        log.info("Получить описание Категории. id {}", userId);
        return mapper.toDto(findById(userId));
    }

    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return repository.findById(id == null ? 0L : id)
                .orElseThrow(() -> new NotFoundException("Запись не найдена"));
    }
}
