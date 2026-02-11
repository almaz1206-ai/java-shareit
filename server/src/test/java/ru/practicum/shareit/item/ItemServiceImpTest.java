package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemServiceImpTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(User.builder()
                        .name("Owner name")
                        .email("owner@example.com")
                        .build());

        anotherUser = userRepository.save(User.builder()
                .name("Another name")
                .email("another@example.com")
                .build());
    }

    @Test
    void getAll_whenUserHasItems_thenReturnItems() {
        Item item1 = itemRepository.save(Item.builder()
                        .name("Дрель")
                        .description("Аккумуляторная дрель")
                        .available(true)
                        .owner(owner)
                        .build());

        Item item2 = itemRepository.save(Item.builder()
                .name("Молоток")
                .description("Профессиональный молоток")
                .available(true)
                .owner(owner)
                .build());

        itemRepository.save(item1);
        itemRepository.save(item2);

        List<ItemDto> res = itemService.getAll(owner.getId());

        assertThat(res).hasSize(2);

        assertThat(res)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Дрель", "Молоток");

        ItemDto item1Dto = res.stream()
                .filter(item -> item.getName().equals("Дрель"))
                .findFirst()
                .orElseThrow();

        assertThat(item1Dto.getDescription()).isEqualTo("Аккумуляторная дрель");
        assertThat(item1Dto.getAvailable()).isTrue();
        assertThat(item1Dto.getOwnerId()).isEqualTo(owner.getId());

        assertThat(item1Dto.getLastBooking()).isNull();
        assertThat(item1Dto.getNextBooking()).isNull();
        assertThat(item1Dto.getComments()).isNullOrEmpty();
    }

    @Test
    void getAll_shouldReturnOnlyOwnersItems() {
        // Вещи владельца
        Item ownersItem1 = Item.builder()
                .name("Вещь владельца 1")
                .description("Описание")
                .available(true)
                .owner(owner)
                .build();

        Item ownersItem2 = Item.builder()
                .name("Вещь владельца 2")
                .description("Описание")
                .available(true)
                .owner(owner)
                .build();

        // Вещи другого пользователя
        Item anothersItem = Item.builder()
                .name("Вещь другого пользователя")
                .description("Описание")
                .available(true)
                .owner(anotherUser)
                .build();

        itemRepository.save(ownersItem1);
        itemRepository.save(ownersItem2);
        itemRepository.save(anothersItem);

        List<ItemDto> result = itemService.getAll(owner.getId());

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ItemDto::getName)
                .containsExactlyInAnyOrder("Вещь владельца 1", "Вещь владельца 2");

        boolean hasAnotherItem = result.stream()
                .anyMatch(item -> item.getName().equals("Вещь другого пользователя"));
        assertThat(hasAnotherItem).isFalse();
    }
}
