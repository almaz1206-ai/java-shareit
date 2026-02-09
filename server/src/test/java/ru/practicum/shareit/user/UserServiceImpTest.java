package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.errors.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImpTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create_whenValidData_thenUserCreated() {
        UserDto userDto = new UserDto(null, "Test user", "test_user@example.com");

        UserDto createdUser = userService.create(userDto);

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Test user");
        assertThat(createdUser.getEmail()).isEqualTo("test_user@example.com");

        assertThat(userRepository.existsById(createdUser.getId())).isTrue();
    }

    @Test
    void create_whenEmailAlreadyExists_thenThrowConflictException() {
        UserDto user1Dto = new UserDto(null, "First user", "first_user@example.com");
        userService.create(user1Dto);

        UserDto user2Dto = new UserDto(null, "Second user", "first_user@example.com");

        assertThatThrownBy(() -> userService.create(user2Dto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Пользователь с email: first_user@example.com уже существует");
    }

    @Test
    void update_whenUpdatingEmail_thenOnlyEmailUpdated() {
        UserDto userDto = new UserDto(null, "Test user", "test_user@example.com");
        UserDto savedUser = userService.create(userDto);

        UserDto updateUser = new UserDto(savedUser.getId(), savedUser.getName(), "test_user1@example.com");
        UserDto updatedUser = userService.update(updateUser, savedUser.getId());

        assertThat(updatedUser.getEmail()).isEqualTo("test_user1@example.com");
        assertThat(updatedUser.getName()).isEqualTo("Test user");

        UserDto userFromDb = userService.getById(savedUser.getId());
        assertThat(userFromDb.getName()).isEqualTo("Test user");
        assertThat(userFromDb.getEmail()).isEqualTo("test_user1@example.com");
    }

    @Test
    void update_whenEmailAlreadyUsedByOtherUser_thenThrowConflictException() {
        UserDto user1Dto = userService.create(new UserDto(null, "Test user1", "test_user1@example.com"));
        UserDto user2Dto = userService.create(new UserDto(null, "Test user2", "test_user2@example.com"));

        UserDto updateUserDto = new UserDto(user2Dto.getId(), user2Dto.getName(), "test_user1@example.com");

        assertThatThrownBy(() -> userService.update(updateUserDto, user2Dto.getId()))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Пользователь с email: test_user1@example.com уже существует");

    }
}
