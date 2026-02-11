package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.errors.NotFoundException;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class RequestServiceImplTest {
    @Autowired
    private ItemRequestServiceImpl itemRequestService;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;

    private User requester;

    @BeforeEach
    void setUp() {
        requester = userRepository.save(User.builder()
                        .name("Requester")
                        .email("requester@example.com")
                        .build());
    }

    @Test
    void create_withValidData_shouldCreateRequest() {
        ItemRequestDto requestDto = new ItemRequestDto("Нужна дрель для ремонта");

        ItemRequestResponseDto createdRequest = itemRequestService.create(requestDto, requester.getId());

        assertThat(createdRequest).isNotNull();
        assertThat(createdRequest.getId()).isNotNull();
        assertThat(createdRequest.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(createdRequest.getRequesterId()).isEqualTo(requester.getId());
        assertThat(createdRequest.getCreated()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(createdRequest.getItems()).isEmpty();

        // Проверяем сохранение в БД
        assertThat(itemRequestRepository.count()).isEqualTo(1);
        var requestFromDb = itemRequestRepository.findById(createdRequest.getId()).orElseThrow();
        assertThat(requestFromDb.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(requestFromDb.getRequester().getId()).isEqualTo(requester.getId());
    }

    @Test
    void create_withNonExistentUser_shouldThrowNotFoundException() {
        ItemRequestDto requestDto = new ItemRequestDto("Нужна вещь");

        assertThatThrownBy(() -> itemRequestService.create(requestDto, 999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void create_multipleRequests_shouldCreateAll() {
        ItemRequestDto request1 = new ItemRequestDto("Первый запрос");

        ItemRequestDto request2 = new ItemRequestDto("Второй запрос");

        ItemRequestResponseDto created1 = itemRequestService.create(request1, requester.getId());
        ItemRequestResponseDto created2 = itemRequestService.create(request2, requester.getId());

        assertThat(itemRequestRepository.count()).isEqualTo(2);

        assertThat(created1.getDescription()).isEqualTo("Первый запрос");
        assertThat(created2.getDescription()).isEqualTo("Второй запрос");
        assertThat(created1.getRequesterId()).isEqualTo(requester.getId());
        assertThat(created2.getRequesterId()).isEqualTo(requester.getId());
    }
}
