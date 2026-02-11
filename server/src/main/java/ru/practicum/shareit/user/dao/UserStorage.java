package ru.practicum.shareit.user.dao;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Optional<User> get(Long id);

    Optional<User> delete(Long id);

    List<User> getAll();
}
