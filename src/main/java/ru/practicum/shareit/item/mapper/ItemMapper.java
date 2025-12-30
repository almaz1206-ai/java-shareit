package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@UtilityClass
public class ItemMapper {

    public ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return new ItemDto().toBuilder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .build();
    }

    public Item toItem(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        return new Item().toBuilder()
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }
}
