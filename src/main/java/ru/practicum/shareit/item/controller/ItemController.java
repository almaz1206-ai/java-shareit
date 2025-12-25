package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@Valid @RequestBody ItemDto itemDto,
                          @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        return ItemMapper.toItemDto(itemService.create(item, userId));
    }

    @GetMapping
    public List<ItemDto> getAll(@RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        List<Item> items = itemService.getAll(userId);
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@PathVariable Long itemId) {
        Item item = itemService.get(itemId);
        return ItemMapper.toItemDto(item);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        itemService.delete(itemId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestBody ItemDto itemDto,
                       @PathVariable Long itemId,
                       @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        Item item = ItemMapper.toItem(itemDto);
        item.setId(itemId);
        return ItemMapper.toItemDto(itemService.update(item, userId));
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(required = false) String text,
                                @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        return itemService.search(text, userId).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

}
