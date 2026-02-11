package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;


    @Override
    public ItemRequestResponseDto create(ItemRequestDto itemRequestDto, Long userId) {
        log.info("Creating request for user id: {}", userId);
        log.info("Request DTO: description={}", itemRequestDto.getDescription());
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        log.info("Found requester: id={}, name={}", requester.getId(), requester.getName());
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequester(requester);
        itemRequest.setCreated(LocalDateTime.now());

        ItemRequest savedItemRequest = itemRequestRepository.save(itemRequest);
        log.info("Request saved with id: {}", savedItemRequest.getId());
        return ItemRequestMapper.toItemRequestResponseDto(savedItemRequest, Collections.emptyList());
    }

    @Override
    public ItemRequestResponseDto getRequestById(Long itemRequestId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        List<Item> items = itemRepository.findByRequestId(itemRequestId);

        return ItemRequestMapper.toItemRequestResponseDto(request, items);
    }

    @Override
    public List<ItemRequestResponseDto> getUserRequests(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        if (requests.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> {
                   List<Item> items = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());
                   return ItemRequestMapper.toItemRequestResponseDto(request, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestResponseDto> getAllRequests(Long userId, Integer from, Integer size) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests;

        if (from == null || size == null) {
            requests = itemRequestRepository.findAllByRequesterIdNot(userId);
        } else {
            requests = itemRequestRepository.findAllByRequesterIdNot(userId, pageable);
        }

        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());

        Map<Long, List<Item>> itemsByRequestId = itemRepository.findByRequestIdIn(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));

        return requests.stream()
                .map(request -> {
                    List<Item> items = itemsByRequestId.getOrDefault(request.getId(), Collections.emptyList());

                    return ItemRequestMapper.toItemRequestResponseDto(request, items);
                })
                .collect(Collectors.toList());
    }

}
