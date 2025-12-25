package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.user.dao.UserStorage;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserStorage userStorage;
    private long id = 0;

    @Override
    public User create(User user) {
        validate(user);
        validateExistEmail(user.getEmail());

        user.setId(generatedId());
        return userStorage.create(user);
    }

    @Override
    public User get(Long id) {
        return userStorage.get(id).orElseThrow(() ->
                new NotFoundException(String.format("Пользователя с id: %s не существует", id)));
    }

    @Override
    public List<User> getAll() {
        return userStorage.getAll();
    }

    @Override
    public User update(User user) {
        validateExistEmail(user.getEmail());

        User updatedUser = get(user.getId());
        if (user.getName() != null) {
            updatedUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            updatedUser.setEmail(user.getEmail());
        }

        return userStorage.update(updatedUser);
    }

    @Override
    public void delete(Long id) {
        userStorage.delete(id);
    }

    private long generatedId() {
        return ++id;
    }

    private void validate(User user) {
        if (user.getEmail() == null) {
            throw new ValidationException("Поле емайл не может быть пустым");
        }
    }

    private void validateExistEmail(String email) {
        if (getAll().stream().anyMatch(user -> user.getEmail().equals(email))) {
            throw new ValidationException(String.format("Пользователь с email: %s уже существует", email));
        }
    }
}
