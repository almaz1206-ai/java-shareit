package ru.practicum.shareit.booking.sevice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResponseDto create(BookingRequestDto bookingDto, Long userId) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать свою вещь");
        }

        if (!item.getAvailable()) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }

        Booking booking = Booking.builder()
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Создано бронирование с ID: {}", savedBooking.getId());

        return BookingMapper.toBookingDto(savedBooking);
    }

    @Override
    public BookingResponseDto updateBookingStatus(Long bookingId, Long userId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);
        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(userId)) {
            throw new ValidationException("Только владелец может подтверждать бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже было обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Статус бронирования id: {} изменен на {}", bookingId, updatedBooking.getStart());
        return BookingMapper.toBookingDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (
                !booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException(String.format("Бронирование с id: %s не найдено или у пользователя нет доступа", bookingId));
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingsByOwner(Long userId, BookingState state) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(String.format("Пользователь с id: %s не найден", userId));
        }

        LocalDateTime now = LocalDateTime.now();
        List<BookingResponseDto> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, now, now)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookingRepository.findByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookingRepository.findByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = bookingRepository.findByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            default:
                bookings = bookingRepository.findByItemOwnerIdOrderByStartDesc(userId)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
        }

        return bookings;
    }

    @Override
    public List<BookingResponseDto> getBookingByBooker(Long bookerId, BookingState state) {
        if (!userRepository.existsById(bookerId)) {
            throw new NotFoundException(String.format("Пользователь с id: %s не найден", bookerId));
        }

        LocalDateTime now = LocalDateTime.now();
        List<BookingResponseDto> bookings;

        switch (state) {
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(bookerId, now, now)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(bookerId, now)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(bookerId, now)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());
                break;
            default:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(bookerId)
                        .stream()
                        .map(BookingMapper::toBookingDto)
                        .collect(Collectors.toList());

        }

        return bookings;
    }


    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с ID: %d не найден", userId)));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с ID: %d не найден", itemId)));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с ID: %d не найден", bookingId)));
    }

}
