package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    Item create(Item item, Long userId);

    Item update(Item item, Long userId);

    Item get(Long id);

    void delete(Long id);

    List<Item> getAll(Long id);

    List<Item> search(String text, Long userId);
}
