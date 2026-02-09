package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.controller.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ItemRequestService itemRequestService;

    private UserDto requester;
    private ItemDto itemDto;
    private ItemRequestResponseDto requestResponseDto;
    private ItemRequestDto requestDto;
    private LocalDateTime createdTime;

    @BeforeEach
    void setUp() {
        createdTime = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        requester = new UserDto(1L, "Requester Name", "requester@example.com");

        itemDto = ItemDto.builder()
                .id(1L)
                .name("Дрель аккумуляторная")
                .description("Мощная дрель для дома")
                .available(true)
                .ownerId(2L)
                .requestId(100L)
                .build();

        requestResponseDto = ItemRequestResponseDto.builder()
                .id(100L)
                .description("Нужна дрель для ремонта")
                .requesterId(requester.getId())
                .created(createdTime)
                .items(List.of(itemDto))
                .build();

        requestDto = new ItemRequestDto("Нужна дрель для ремонта");
    }

    @Test
    void createRequest_whenNoUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .content(objectMapper.writeValueAsString(requestDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_whenNoBody_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserRequests_whenUserHasRequests_thenReturnRequests() throws Exception {
        ItemRequestResponseDto request1 = ItemRequestResponseDto.builder()
                .id(100L)
                .description("Нужна дрель")
                .requesterId(requester.getId())
                .created(createdTime.minusDays(1))
                .items(List.of(itemDto))
                .build();

        ItemRequestResponseDto request2 = ItemRequestResponseDto.builder()
                .id(101L)
                .description("Нужен молоток")
                .requesterId(requester.getId())
                .created(createdTime)
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getUserRequests(1L))
                .thenReturn(List.of(request1, request2));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].description", is("Нужна дрель")))
                .andExpect(jsonPath("$[0].items", hasSize(1)))
                .andExpect(jsonPath("$[1].id", is(101)))
                .andExpect(jsonPath("$[1].description", is("Нужен молоток")))
                .andExpect(jsonPath("$[1].items", hasSize(0)));
    }

    @Test
    void getUserRequests_whenUserHasNoRequests_thenReturnEmptyList() throws Exception {
        when(itemRequestService.getUserRequests(1L))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getUserRequests_whenNoUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_withoutPagination_thenReturnRequests() throws Exception {
        UserDto otherUser = new UserDto(2L, "Other User", "other@example.com");

        ItemRequestResponseDto request = ItemRequestResponseDto.builder()
                .id(100L)
                .description("Запрос без пагинации")
                .requesterId(otherUser.getId())
                .created(createdTime)
                .items(Collections.emptyList())
                .build();

        when(itemRequestService.getAllRequests(eq(1L), eq(null), eq(null)))
                .thenReturn(List.of(request));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)));
    }

    @Test
    void getAllRequests_withNegativePaginationParams_shouldWork() throws Exception {
        when(itemRequestService.getAllRequests(eq(1L), eq(-1), eq(-10)))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "-10"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_whenNoUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_whenRequestExists_thenReturnRequest() throws Exception {
        when(itemRequestService.getRequestById(100L, 1L))
                .thenReturn(requestResponseDto);

        mockMvc.perform(get("/requests/{requestId}", 100L)
                        .header("X-Sharer-User-Id", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(100)))
                .andExpect(jsonPath("$.description", is("Нужна дрель для ремонта")))
                .andExpect(jsonPath("$.requesterId", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id", is(1)));
    }

    @Test
    void getRequestById_whenRequestNotExists_thenReturnNotFound() throws Exception {
        when(itemRequestService.getRequestById(999L, 1L))
                .thenThrow(new ru.practicum.shareit.errors.NotFoundException("Запрос не найден"));

        mockMvc.perform(get("/requests/{requestId}", 999L)
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestById_whenNoUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/{requestId}", 100L))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequestById_withInvalidPathId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/invalid-id")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isBadRequest());
    }
}
