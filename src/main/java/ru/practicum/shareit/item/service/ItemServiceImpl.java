package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.AccessDeniedException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    private long id = 0;

    @Override
    public Item create(Item item, Long userId) {
        validate(item);

        if (userId == null) {
            throw new ValidationException("userId не может быть null");
        }

        User owner = userService.get(userId);
        item.setOwner(owner);

        item.setId(generatedId());
        return itemStorage.create(item);
    }

    @Override
    public List<Item> getAll(Long userId) {
        return itemStorage.getAll()
                .stream()
                .filter(Objects::nonNull)
                .filter(item -> {
                    if (item.getOwner() == null) {
                        return false;
                    }
                    return item.getOwner().getId().equals(userId);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Item get(Long id) {
        return itemStorage.get(id)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", id)));
    }

    @Override
    public Item update(Item item, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id пользователя не может быть null");
        }
        Item existingItem = get(item.getId());

        User owner = existingItem.getOwner();

        if (owner == null || !owner.getId().equals(userId)) {
            throw new AccessDeniedException(
                    String.format("Предмет с id: %s принадлежит другому пользователю", item.getOwner()));
        }

        if (item.getName() != null) {
            existingItem.setName(item.getName());
        }

        if (item.getDescription() != null) {
            existingItem.setDescription(item.getDescription());
        }

        if (item.getAvailable() != null) {
            existingItem.setAvailable(item.getAvailable());
        }

        return itemStorage.update(item);
    }

    @Override
    public void delete(Long id) {
        itemStorage.delete(id)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", id)));
    }

    @Override
    public List<Item> search(String text, Long userId) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemStorage.getAll()
                .stream()
                .filter(Objects::nonNull)
                .filter(item -> {
                    Boolean available = item.getAvailable();
                    return available != null && available;
                })
                .filter(item -> item.getDescription() != null
                        && item.getDescription().toLowerCase().contains(text.toLowerCase())
                        || item.getName() != null
                        && item.getName().toLowerCase().contains(text.toLowerCase())
                ).collect(Collectors.toList());
    }


    private long generatedId() {
        return ++id;
    }

    private void validate(Item item) {
        if (item.getName() == null || item.getName().isBlank()) {
            throw new ValidationException("Наименование не может быть пусты!");
        }

        if (item.getDescription() == null || item.getDescription().isBlank()) {
            throw new ValidationException("Описание не может быть пустым");
        }

        if (item.getAvailable() == null) {
            throw new ValidationException("Статус доступности не может быть null");
        }
    }
}
