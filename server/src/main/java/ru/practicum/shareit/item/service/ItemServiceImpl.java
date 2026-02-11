package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.errors.AccessDeniedException;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.errors.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        log.info("Создаем вещь для пользователя с id: {}", userId);
        log.info("Item DTO: name={}, description={}, available={}, requestId={}",
                itemDto.getName(), itemDto.getDescription(),
                itemDto.getAvailable(), itemDto.getRequestId());
        if (userId == null) {
            throw new ValidationException("userId не может быть null");
        }

        User owner = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        log.info("Mapped item: {}", item);
        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    public List<ItemDto> getAll(Long userId) {
        return itemRepository.findAllByOwnerId(userId)
                .stream()
                .filter(item -> {
                    if (item.getOwner() == null) {
                        return false;
                    }
                    return item.getOwner().getId().equals(userId);
                })
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto get(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", id)));

        BookingResponseDto lastBooking = null;
        BookingResponseDto nextBooking = null;

        if (userId.equals(item.getOwner().getId())) {
            List<BookingResponseDto> allBookings = bookingRepository
                    .findByItemId(id).stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

            lastBooking = getLastItem(allBookings);
            nextBooking = getNextItem(allBookings);
        }

        Map<Long, List<CommentDto>> comments = getAllComments().stream().collect(Collectors.groupingBy(CommentDto::getItemId));
        return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments.get(id));
    }

    @Override
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id пользователя не может быть null");
        }
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException(String.format("Предмета с id: %s не существует", itemDto.getId())));

        User owner = existingItem.getOwner();

        if (owner == null || !owner.getId().equals(userId)) {
            throw new AccessDeniedException(
                    String.format("Предмет с id: %s принадлежит другому пользователю", existingItem.getOwner()));
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public void delete(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new NotFoundException(String.format("Предмет с id: %s не найден", id));
        }
        itemRepository.deleteById(id);
    }

    @Override
    public List<ItemDto> search(String text, Long userId) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableItems(text.toLowerCase())
                .stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto createComment(CommentDto commentDto, Long itemId, Long userId) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Комментарий не может быть пустым");
        }

        User author = userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId).orElseThrow(() ->
                new NotFoundException("Вещь не найдена"));

        List<Booking> bookings = bookingRepository
                .findCompletedBookingsForComment(userId, itemId, BookingStatus.APPROVED, LocalDateTime.now());
        log.info("Found {} completed bookings for user {} and item {}", bookings.size(), userId, itemId);
        if (bookings.isEmpty()) {
            throw new ValidationException(
                    "Вы не можете оставить отзыв на вещь, которую не брали в аренду или аренда ещё не завершена");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    public List<CommentDto> getAllComments() {
        return commentRepository.findAll()
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private BookingResponseDto getLastItem(List<BookingResponseDto> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }

        return bookings.stream()
                .filter(booking -> booking.getEnd().isBefore(LocalDateTime.now()))
                .max(Comparator.comparing(BookingResponseDto::getEnd))
                .orElse(null);
    }

    private BookingResponseDto getNextItem(List<BookingResponseDto> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            return null;
        }
        return bookings.stream()
                .filter(booking -> booking.getStart().isAfter(LocalDateTime.now()))
                .min(Comparator.comparing(BookingResponseDto::getEnd))
                .orElse(null);
    }
}
