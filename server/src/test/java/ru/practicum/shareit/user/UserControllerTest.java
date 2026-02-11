package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserDto userDto;
    private UserDto createdUserDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(null, "John Doe", "john.doe@example.com");

        createdUserDto = new UserDto(1L, "John Doe", "john.doe@example.com");
    }

    @Test
    void create_whenValidData_thenReturnCreatedUser() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenReturn(createdUserDto);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void create_whenDuplicateEmail_thenReturnConflict() throws Exception {
        when(userService.create(any(UserDto.class)))
                .thenThrow(new ru.practicum.shareit.errors.ConflictException("Email уже существует"));

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void create_whenNoBody_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAll_whenUsersExist_thenReturnUsers() throws Exception {
        UserDto user1 = new UserDto(1L, "John Doe", "john@example.com");
        UserDto user2 = new UserDto(2L, "Jane Smith", "jane@example.com");

        when(userService.getAll())
                .thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("John Doe")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].name", is("Jane Smith")));
    }

    @Test
    void getAll_whenNoUsers_thenReturnEmptyList() throws Exception {
        when(userService.getAll())
                .thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void get_whenUserExists_thenReturnUser() throws Exception {
        when(userService.getById(1L))
                .thenReturn(createdUserDto);

        mockMvc.perform(get("/users/{userId}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Doe")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")));
    }

    @Test
    void get_whenUserNotExists_thenReturnNotFound() throws Exception {
        when(userService.getById(999L))
                .thenThrow(new ru.practicum.shareit.errors.NotFoundException("Пользователь не найден"));

        mockMvc.perform(get("/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_withInvalidPathId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/invalid-id"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_whenNullId_thenReturnBadRequest() throws Exception {
        when(userService.getById(null))
                .thenThrow(new ru.practicum.shareit.errors.ValidationException("ID не может быть null"));

        mockMvc.perform(get("/users/{userId}", "null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_whenUpdatingName_thenReturnUpdatedUser() throws Exception {
        UserDto updateDto = new UserDto(null, "John Updated", null);
        UserDto updatedUser = new UserDto(1L, "John Updated", "john.doe@example.com");

        when(userService.update(any(UserDto.class), eq(1L)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John Updated")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com"))); // Email не изменился
    }

    @Test
    void update_whenUpdatingEmail_thenReturnUpdatedUser() throws Exception {
        UserDto updateDto = new UserDto(null, null, "new.email@example.com");
        UserDto updatedUser = new UserDto(1L, "John Doe", "new.email@example.com");

        when(userService.update(any(UserDto.class), eq(1L)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe"))) // Имя не изменилось
                .andExpect(jsonPath("$.email", is("new.email@example.com")));
    }

    @Test
    void update_allFields_thenReturnUpdatedUser() throws Exception {
        UserDto updateDto = new UserDto(null, "New Name", "new.email@example.com");
        UserDto updatedUser = new UserDto(1L, "New Name", "new.email@example.com");

        when(userService.update(any(UserDto.class), eq(1L)))
                .thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("New Name")))
                .andExpect(jsonPath("$.email", is("new.email@example.com")));
    }

    @Test
    void update_whenUserNotExists_thenReturnNotFound() throws Exception {
        UserDto updateDto = new UserDto(null, "New Name", null);

        when(userService.update(any(UserDto.class), eq(999L)))
                .thenThrow(new ru.practicum.shareit.errors.NotFoundException("Пользователь не найден"));

        mockMvc.perform(patch("/users/{userId}", 999L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_whenDuplicateEmail_thenReturnConflict() throws Exception {
        UserDto updateDto = new UserDto(null, null, "existing@example.com");

        when(userService.update(any(UserDto.class), eq(1L)))
                .thenThrow(new ru.practicum.shareit.errors.ConflictException("Email уже существует"));

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .content(objectMapper.writeValueAsString(updateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void update_withEmptyBody_shouldWork() throws Exception {
        UserDto emptyUpdateDto = new UserDto(null, null, null);

        when(userService.update(any(UserDto.class), eq(1L)))
                .thenReturn(createdUserDto);

        mockMvc.perform(patch("/users/{userId}", 1L)
                        .content(objectMapper.writeValueAsString(emptyUpdateDto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void update_withInvalidPathId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/users/invalid-id")
                        .content("{\"name\":\"New Name\"}")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_whenUserExists_thenReturnOk() throws Exception {
        mockMvc.perform(delete("/users/{userId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void delete_whenUserNotExists_thenReturnNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ru.practicum.shareit.errors.NotFoundException("Пользователь не найден"))
                .when(userService).delete(999L);

        mockMvc.perform(delete("/users/{userId}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_whenNullId_thenReturnBadRequest() throws Exception {
        org.mockito.Mockito.doThrow(new ru.practicum.shareit.errors.ValidationException("ID не может быть null"))
                .when(userService).delete(null);

        mockMvc.perform(delete("/users/{userId}", "null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_withInvalidPathId_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(delete("/users/invalid-id"))
                .andExpect(status().isBadRequest());
    }
}
