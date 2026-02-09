package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.sevice.BookingService;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(
               null,
               "Owner",
               "owner@example.com"));

        booker = userRepository.save(new User(
                null,
                "Booker",
                "booker@example.com"
        ));

        item = itemRepository.save(Item.builder()
                        .name("Дрель")
                        .description("Аккумуляторная дрель")
                        .available(true)
                        .owner(owner)
                .build());
    }

    @Test
    void create_withValidData_shouldCreateBooking() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(2);

        BookingRequestDto requestDto = new BookingRequestDto(item.getId(), start, end);

        BookingResponseDto createdBooking = bookingService.create(requestDto, booker.getId());

        assertThat(createdBooking).isNotNull();
        assertThat(createdBooking.getId()).isNotNull();
        assertThat(createdBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(createdBooking.getStart()).isEqualTo(start);
        assertThat(createdBooking.getEnd()).isEqualTo(end);
        assertThat(createdBooking.getItem().getId()).isEqualTo(item.getId());
        assertThat(createdBooking.getBooker().getId()).isEqualTo(booker.getId());

        // Проверяем сохранение в БД
        Booking savedBooking = bookingRepository.findById(createdBooking.getId()).orElseThrow();
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(savedBooking.getItem().getId()).isEqualTo(item.getId());
        assertThat(savedBooking.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void create_whenOwnerBooksOwnItem_shouldThrowNotFoundException() {
        BookingRequestDto requestDto = new BookingRequestDto(
                item.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(requestDto, owner.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Владелец не может бронировать свою вещь");
    }

    @Test
    void create_whenItemUnavailable_shouldThrowValidationException() {
        Item unavailableItem = itemRepository.save(Item.builder()
                .name("Недоступная вещь")
                .description("Описание")
                .available(false) // Недоступна!
                .owner(owner)
                .build());

        BookingRequestDto requestDto = new BookingRequestDto(
                unavailableItem.getId(),
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        assertThatThrownBy(() -> bookingService.create(requestDto, booker.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Вещь недоступна для бронирования");
    }
}
