package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.sevice.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingResponseDto create(
            @RequestBody @Valid BookingRequestDto bookingDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.create(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBookingStatus(
        @PathVariable long bookingId,
        @RequestParam Boolean approved,
        @RequestHeader("X-Sharer-User-Id") Long userId) {

        return bookingService.updateBookingStatus(bookingId, userId, approved);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingsForOwnerItems(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "ALL")BookingState state) {

        return bookingService.getBookingsByOwner(ownerId, state);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingByBooker(
            @RequestHeader("X-Sharer-User-Id") Long bookerId,
            @RequestParam(defaultValue = "ALL") BookingState state) {
        return bookingService.getBookingsByBooker(bookerId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(@PathVariable Long bookingId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBookingById(bookingId, userId);
    }

}
