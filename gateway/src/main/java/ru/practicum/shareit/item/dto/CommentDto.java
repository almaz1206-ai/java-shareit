package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CommentDto {
    private Long id;

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 2000, message = "Текст не должен превышать 2000 символов")
    private String text;
    private String authorName;
    private LocalDateTime created;
}
