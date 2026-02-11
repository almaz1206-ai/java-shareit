package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

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
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public Item toItem(ItemDto itemDto) {
        if (itemDto == null) {
            return null;
        }

        return new Item().toBuilder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }

    public ItemDto toItemDto(Item item, BookingResponseDto lastBooking, BookingResponseDto nextBooking, List<CommentDto> comments) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .ownerId(item.getOwner() != null ? item.getOwner().getId() : null)
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .lastBooking(lastBooking != null ? new BookingDto(lastBooking.getId(), lastBooking.getBooker().getId()) : null)
                .nextBooking(nextBooking != null ? new BookingDto(nextBooking.getId(), nextBooking.getBooker().getId()) : null)
                .comments(comments != null ? comments : List.of())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }
}
