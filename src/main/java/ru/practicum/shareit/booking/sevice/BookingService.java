package ru.practicum.shareit.booking.sevice;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;

import java.util.List;

public interface BookingService {
    BookingResponseDto create(BookingRequestDto bookingDto, Long userId);

    BookingResponseDto updateBookingStatus(Long bookingId, Long userId, Boolean approved);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingByBooker(Long userId, BookingState state);

    List<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state);
}
