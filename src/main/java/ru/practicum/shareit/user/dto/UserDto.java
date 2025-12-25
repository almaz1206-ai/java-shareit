package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDto {
    private final Long id;
    private final String name;
    @Email(message = "Некорректный email адрес")
    @NotBlank(message = "Поле email обязателен для заполнения")
    private final String email;
}
