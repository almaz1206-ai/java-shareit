package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @NotBlank(message = "Имя обязательно для заполнения")
    private String name;
    @Email(message = "Некорректный email адрес")
    @NotBlank(message = "Поле email обязетелен для заполнения")
    private String email;
}
