package ru.practicum.shareit.booking.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.user.mapper.UserMapper;

@UtilityClass
public class BookingMapper {

    public static Booking toBooking(BookingResponseDto bookingDto) {
        if (bookingDto == null) {
            return null;
        }

        return Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .build();
    }

    public static BookingResponseDto toBookingDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingResponseDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .item(booking.getItem() != null ? ItemMapper.toItemDto(booking.getItem()) : null)
                .booker(booking.getBooker() != null ? UserMapper.toUserDto(booking.getBooker()) : null)
                .status(booking.getStatus())
                .build();
    }
}
