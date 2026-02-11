package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.controller.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemService itemService;

    private ItemDto itemDto;
    private ItemDto itemDtoWithDetails;
    private CommentDto commentDto;

    @BeforeEach
    void setUp() {
        itemDto = ItemDto.builder()
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .requestId(1L)
                .build();

        BookingDto lastBooking = BookingDto.builder()
                .id(1L)
                .bookerId(10L)
                .build();

        BookingDto nextBooking = BookingDto.builder()
                .id(2L)
                .bookerId(11L)
                .build();

        itemDtoWithDetails = ItemDto.builder()
                .id(100L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .ownerId(1L)
                .requestId(5L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(List.of(
                        CommentDto.builder()
                                .id(50L)
                                .text("Отличная вещь!")
                                .authorName("Пользователь")
                                .created(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                                .build()
                ))
                .build();

        commentDto = CommentDto.builder()
                .id(50L)
                .text("Отличная вещь!")
                .authorName("Пользователь")
                .created(LocalDateTime.of(2024, 1, 1, 12, 0, 0))
                .build();
    }

    @Test
    void createItem_whenValidData_thenReturnCreatedItem() throws Exception {
        ItemDto createdItem = ItemDto.builder()
                .id(100L)
                .name("Дрель")
                .description("Аккумуляторная дрель")
                .available(true)
                .ownerId(1L)
                .requestId(5L)
                .build();

        when(itemService.create(any(ItemDto.class), eq(1L)))
                .thenReturn(createdItem);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.description", is("Аккумуляторная дрель")))
                .andExpect(jsonPath("$.available", is(true)))
                .andExpect(jsonPath("$.ownerId", is(1)))
                .andExpect(jsonPath("$.requestId", is(5)));
    }

    @Test
    void getAll_whenUserHasNoItems_thenReturnEmptyList() throws Exception {
        when(itemService.getAll(1L))
                .thenReturn(List.of());

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void get_whenItemExists_thenReturnItem() throws Exception {
        when(itemService.get(100L, 1L))
                .thenReturn(itemDtoWithDetails);

        mockMvc.perform(get("/items/{itemId}", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Дрель")))
                .andExpect(jsonPath("$.ownerId", is(1)))
                .andExpect(jsonPath("$.lastBooking.id", is(1)))
                .andExpect(jsonPath("$.lastBooking.bookerId", is(10)))
                .andExpect(jsonPath("$.nextBooking.id", is(2)))
                .andExpect(jsonPath("$.nextBooking.bookerId", is(11)))
                .andExpect(jsonPath("$.comments", hasSize(1)))
                .andExpect(jsonPath("$.comments[0].text", is("Отличная вещь!")));
    }

    @Test
    void get_withoutUserIdHeader_shouldWork() throws Exception {
        ItemDto itemWithoutBookings = ItemDto.builder()
                .id(100L)
                .name("Дрель")
                .description("Описание")
                .available(true)
                .ownerId(1L)
                .build();

        when(itemService.get(100L, null))
                .thenReturn(itemWithoutBookings);

        mockMvc.perform(get("/items/{itemId}", 100L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.lastBooking").doesNotExist())
                .andExpect(jsonPath("$.nextBooking").doesNotExist());
    }

    @Test
    void get_whenItemNotExists_thenReturnNotFound() throws Exception {
        when(itemService.get(999L, 1L))
                .thenThrow(new ru.practicum.shareit.errors.NotFoundException("Вещь не найдена"));

        mockMvc.perform(get("/items/{itemId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_withInvalidPathId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/invalid-id")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_whenPartialUpdate_thenReturnUpdatedItem() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Новое имя дрели")
                .available(false)
                .build();

        ItemDto updatedItem = ItemDto.builder()
                .id(100L)
                .name("Новое имя дрели")
                .description("Аккумуляторная дрель") // Старое описание
                .available(false) // Обновлено
                .ownerId(1L)
                .build();

        when(itemService.update(any(ItemDto.class), eq(100L), eq(1L)))
                .thenReturn(updatedItem);

        mockMvc.perform(patch("/items/{itemId}", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.name", is("Новое имя дрели")))
                .andExpect(jsonPath("$.description", is("Аккумуляторная дрель")))
                .andExpect(jsonPath("$.available", is(false)));
    }

    @Test
    void update_whenNotOwner_thenReturnForbidden() throws Exception {
        ItemDto updateDto = ItemDto.builder()
                .name("Новое имя")
                .build();

        when(itemService.update(any(ItemDto.class), eq(100L), eq(2L)))
                .thenThrow(new ru.practicum.shareit.errors.AccessDeniedException("Нет доступа"));

        mockMvc.perform(patch("/items/{itemId}", 100L)
                        .header("X-Sharer-User-Id", 2L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void update_withEmptyBody_shouldWork() throws Exception {
        ItemDto emptyUpdateDto = ItemDto.builder().build();

        when(itemService.update(any(ItemDto.class), eq(100L), eq(1L)))
                .thenReturn(itemDtoWithDetails);

        mockMvc.perform(patch("/items/{itemId}", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(emptyUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void delete_whenItemExists_thenReturnNoContent() throws Exception {
        mockMvc.perform(delete("/items/{itemId}", 100L))
                .andExpect(status().isOk()); // или isNoContent() в зависимости от реализации
    }

    @Test
    void delete_whenItemNotExists_thenReturnNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ru.practicum.shareit.errors.NotFoundException("Вещь не найдена"))
                .when(itemService).delete(999L);

        mockMvc.perform(delete("/items/{itemId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_withInvalidPathId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/items/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_whenTextMatches_thenReturnItems() throws Exception {
        ItemDto foundItem = ItemDto.builder()
                .id(100L)
                .name("Аккумуляторная дрель")
                .description("Мощная дрель")
                .available(true)
                .ownerId(2L)
                .build();

        when(itemService.search(eq("дрель"), eq(1L)))
                .thenReturn(List.of(foundItem));

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "дрель")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].name", is("Аккумуляторная дрель")));
    }

    @Test
    void search_whenEmptyText_thenReturnEmptyList() throws Exception {
        when(itemService.search(eq(""), eq(1L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void search_withoutTextParam_shouldWork() throws Exception {
        when(itemService.search(eq(null), eq(1L)))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void createComment_whenValidData_thenReturnCreatedComment() throws Exception {
        CommentDto requestComment = CommentDto.builder()
                .text("Отличная вещь!")
                .build();

        when(itemService.createComment(any(CommentDto.class), eq(100L), eq(1L)))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/{itemId}/comment", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(requestComment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(50)))
                .andExpect(jsonPath("$.text", is("Отличная вещь!")))
                .andExpect(jsonPath("$.authorName", is("Пользователь")))
                .andExpect(jsonPath("$.created").exists());
    }

    @Test
    void createComment_whenItemNotExists_thenReturnNotFound() throws Exception {
        CommentDto requestComment = CommentDto.builder()
                .text("Комментарий")
                .build();

        when(itemService.createComment(any(CommentDto.class), eq(999L), eq(1L)))
                .thenThrow(new ru.practicum.shareit.errors.NotFoundException("Вещь не найдена"));

        mockMvc.perform(post("/items/{itemId}/comment", 999L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(requestComment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_whenNoBooking_thenReturnBadRequest() throws Exception {
        CommentDto requestComment = CommentDto.builder()
                .text("Комментарий")
                .build();

        when(itemService.createComment(any(CommentDto.class), eq(100L), eq(1L)))
                .thenThrow(new ru.practicum.shareit.errors.ValidationException("Не было бронирования"));

        mockMvc.perform(post("/items/{itemId}/comment", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .content(objectMapper.writeValueAsString(requestComment))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
