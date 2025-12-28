package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto item, Long userId);

    ItemDto update(ItemDto item, Long itemId, Long userId);

    ItemDto get(Long id);

    void delete(Long id);

    List<ItemDto> getAll(Long id);

    List<ItemDto> search(String text, Long userId);
}
