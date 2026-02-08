package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestBody @Valid ItemDto itemDto,
                                             @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        return itemClient.create(userId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAll(@RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        return itemClient.getAll(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> get(
            @PathVariable Long itemId,
            @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        return itemClient.getItemById(userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> get(@PathVariable Long itemId) {
        return itemClient.delete(itemId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestBody ItemDto itemDto,
            @PathVariable Long itemId,
            @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId
    ) {
        return itemClient.update(itemDto, itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam(required = false) String text,
                                         @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId) {
        return itemClient.search(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> createComment(
            @RequestBody CommentDto commentDto,
            @PathVariable Long itemId,
            @RequestHeader(required = false, value = "X-Sharer-User-Id") Long userId
            ) {
        return itemClient.createComment(commentDto, itemId, userId);
    }
}
