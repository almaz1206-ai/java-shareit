package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.AccessDeniedException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.item.dao.ItemStorage;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;
    private long id = 0;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {

        if (userId == null) {
            throw new ValidationException("userId не может быть null");
        }

        User owner = UserMapper.toUser(userService.get(userId));
        Item item = ItemMapper.toItem(itemDto);
        item.setId(generatedId());
        item.setOwner(owner);

        Item savedItem = itemStorage.create(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        return itemStorage.getAll()
                .stream()
                .filter(item -> {
                    if (item.getOwner() == null) {
                        return false;
                    }
                    return item.getOwner().getId().equals(userId);
                })
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto get(Long id) {
        Item item = itemStorage.get(id)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", id)));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id пользователя не может быть null");
        }
        Item existingItem = itemStorage.get(itemId)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", itemDto.getId())));

        User owner = existingItem.getOwner();

        if (owner == null || !owner.getId().equals(userId)) {
            throw new AccessDeniedException(
                    String.format("Предмет с id: %s принадлежит другому пользователю", existingItem.getOwner()));
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemStorage.update(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public void delete(Long id) {
        itemStorage.delete(id)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", id)));
    }

    @Override
    public List<ItemDto> search(String text, Long userId) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemStorage.getAll()
                .stream()
                .filter(item -> {
                    Boolean available = item.getAvailable();
                    return available != null && available;
                })
                .filter(item -> item.getDescription().toLowerCase().contains(text.toLowerCase())
                        || item.getName().toLowerCase().contains(text.toLowerCase())
                )
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }


    private long generatedId() {
        return ++id;
    }
}
