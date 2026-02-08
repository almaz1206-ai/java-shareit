package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.ConflictException;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.info("Создаем клиента с емайл: {}", userDto.getEmail());
        log.info("UserDto: {}", userDto);

            validateExistEmail(userDto.getEmail(), userDto.getId());
            log.info("Валидация емайл");

            User user = UserMapper.toUser(userDto);
            log.info("Mapped User entity: {}", user);

            User savedUser = userRepository.save(user);
            log.info("Saved User entity - ID: {}", savedUser.getId());

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(Long userId) {
        if (userId == null) throw new ValidationException("Id пользователя не может быть null");

        User user = findUserById(userId);
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto update(UserDto userDto, Long userId) {
        User existingUser = findUserById(userId);

        validateExistEmail(userDto.getEmail(), userId);

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            existingUser.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userRepository.save(existingUser));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new ValidationException("Id пользователя не может быть null");
        if (!userRepository.existsById(id)) {
            throw new NotFoundException(String.format("Пользователя с id: %s не существует", id));
        }
        userRepository.deleteById(id);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException(String.format("Пользователя с id: %s не существует", userId)));
    }

    private void validateExistEmail(String email, Long userId) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new ConflictException(String.format("Пользователь с email: %s уже существует", email));
        }
    }
}
