package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.ConflictException;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private long id = 0;

    @Override
    public UserDto create(UserDto userDto) {
        validateExistEmail(userDto.getEmail());
        User user = UserMapper.toUser(userDto);

        user.setId(generatedId());

        User savedUser = userStorage.create(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto get(Long id) {
        User user = userStorage.get(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователя с id: %s не существует", id)));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.getAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto update(UserDto userDto, Long userId) {
        User existingUser = userStorage.get(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователя с id: %s не существует", id)));
        validateExistEmail(userDto.getEmail());

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            existingUser.setEmail(userDto.getEmail());
        }

        User updatedUser = userStorage.update(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public void delete(Long id) {
        userStorage.delete(id);
    }

    private long generatedId() {
        return ++id;
    }

    private void validateExistEmail(String email) {
        if (getAll().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new ConflictException(String.format("Пользователь с email: %s уже существует", email));
        }
    }
}
