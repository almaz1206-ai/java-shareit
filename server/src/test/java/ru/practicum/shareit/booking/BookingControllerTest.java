package ru.practicum.shareit.booking;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingState;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.sevice.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookingService bookingService;

    private BookingRequestDto bookingRequestDto;
    private BookingResponseDto bookingResponseDto;
    private BookingResponseDto approvedBookingResponseDto;

    private final Long userId = 1L;
    private final Long bookingId = 1L;
    private final Long itemId = 1L;
    private final Long ownerId = 2L;

    @BeforeEach
    void setUp() {
        bookingRequestDto = new BookingRequestDto(
                itemId,
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(2)
        );

        UserDto bookerDto = new UserDto(
                userId,
                "John",
                "john@example.com"
        );

        UserDto ownerDto = new UserDto(
                ownerId,
                "Alice",
                "alice@example.com"
        );

        ItemDto itemDto = ItemDto.builder()
                .id(itemId)
                .name("Дрель")
                .description("Мощная дрель")
                .available(true)
                .requestId(null)
                .ownerId(ownerId)
                .build();

        bookingResponseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .item(itemDto)
                .booker(bookerDto)
                .status(BookingStatus.WAITING)
                .build();

        approvedBookingResponseDto = BookingResponseDto.builder()
                .id(bookingId)
                .start(bookingRequestDto.getStart())
                .end(bookingRequestDto.getEnd())
                .item(itemDto)
                .booker(bookerDto)
                .status(BookingStatus.APPROVED)
                .build();
    }

    @Test
    void create_ShouldCreateBooking() throws Exception {
        when(bookingService.create(any(BookingRequestDto.class), eq(userId)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.toString())))
                .andExpect(jsonPath("$.item.id", is(itemId), Long.class))
                .andExpect(jsonPath("$.item.name", is("Дрель")))
                .andExpect(jsonPath("$.booker.id", is(userId), Long.class))
                .andExpect(jsonPath("$.booker.name", is("John")));
    }

    @Test
    void create_ShouldReturnBadRequestWhenMissingUserIdHeader() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookingRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBookingStatus_ShouldApproveBooking() throws Exception {
        when(bookingService.updateBookingStatus(eq(bookingId), eq(ownerId), eq(true)))
                .thenReturn(approvedBookingResponseDto);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.APPROVED.toString())));
    }

    @Test
    void updateBookingStatus_ShouldRejectBooking() throws Exception {
        BookingResponseDto rejectedBooking = bookingResponseDto.toBuilder()
                .status(BookingStatus.REJECTED)
                .build();

        when(bookingService.updateBookingStatus(eq(bookingId), eq(ownerId), eq(false)))
                .thenReturn(rejectedBooking);

        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .param("approved", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.REJECTED.toString())));
    }

    @Test
    void updateBookingStatus_ShouldReturnBadRequestWhenMissingApprovedParam() throws Exception {
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForOwnerItems_ShouldReturnBookingsWithPagination() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        when(bookingService.getBookingsByOwner(eq(ownerId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId), Long.class))
                .andExpect(jsonPath("$[0].status", is(BookingStatus.WAITING.toString())));
    }

    @Test
    void getBookingsForOwnerItems_ShouldUseDefaultParameters() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        when(bookingService.getBookingsByOwner(eq(ownerId), eq(BookingState.ALL), eq(0), eq(10)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getBookingsForOwnerItems_ShouldReturnBadRequestForInvalidFromParameter() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingsForOwnerItems_ShouldReturnBadRequestForInvalidSizeParameter() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", ownerId)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookingByBooker_ShouldReturnBookings() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        when(bookingService.getBookingsByBooker(eq(userId), eq(BookingState.CURRENT)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "CURRENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingId), Long.class));
    }

    @Test
    void getBookingByBooker_ShouldUseDefaultState() throws Exception {
        List<BookingResponseDto> bookings = List.of(bookingResponseDto);

        when(bookingService.getBookingsByBooker(eq(userId), eq(BookingState.ALL)))
                .thenReturn(bookings);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void getBookingById_ShouldReturnBooking() throws Exception {
        when(bookingService.getBookingById(eq(bookingId), eq(userId)))
                .thenReturn(bookingResponseDto);

        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingId), Long.class))
                .andExpect(jsonPath("$.status", is(BookingStatus.WAITING.toString())))
                .andExpect(jsonPath("$.booker.id", is(userId), Long.class));
    }

    @Test
    void getBookingById_ShouldReturnBadRequestWhenMissingUserIdHeader() throws Exception {
        mockMvc.perform(get("/bookings/{bookingId}", bookingId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEndpoints_ShouldReturnJsonContentType() throws Exception {
        when(bookingService.getBookingsByBooker(anyLong(), any(BookingState.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(result -> result.getResponse().getContentType().contains(MediaType.APPLICATION_JSON_VALUE));
    }
}
