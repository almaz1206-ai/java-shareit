package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestResponseDtoJsonTest {
    private final JacksonTester<ItemRequestResponseDto> json;

    @Test
    void testSerialize() throws Exception {
        ItemDto item1Dto = ItemDto.builder()
                .id(1L)
                .name("Test item 1")
                .description("Test item 1 description")
                .available(true)
                .ownerId(20L)
                .build();

        ItemDto item2Dto = ItemDto.builder()
                .id(2L)
                .name("Test item 2 description")
                .available(true)
                .ownerId(20L)
                .build();

        List<ItemDto> items = List.of(item1Dto, item2Dto);

        ItemRequestResponseDto dto = ItemRequestResponseDto.builder()
                .id(1L)
                .description("Test description")
                .requesterId(10L)
                .created(LocalDateTime.now())
                .items(items)
                .build();

        JsonContent<ItemRequestResponseDto> result = json.write(dto);
        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.requesterId");
        assertThat(result).hasJsonPath("$.created");
        assertThat(result).hasJsonPath("$.items");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathNumberValue("$.requesterId")
                .isEqualTo(dto.getRequesterId().intValue());
        assertThat(result).hasJsonPathValue("$.created");

        assertThat(result).hasJsonPath("$.items[0]");
        assertThat(result).hasJsonPath("$.items[0].id");
        assertThat(result).hasJsonPath("$.items[0].name");
        assertThat(result).hasJsonPath("$.items[0].available");
        assertThat(result).hasJsonPath("$.items[0].ownerId");
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = "{\n" +
                    " \"id\": 1,\n" +
                    " \"description\": \"Test description\",\n" +
                    " \"requesterId\": 10,\n" +
                    " \"created\": \"2026-02-10T21:00:00\",\n" +
                    " \"items\": [\n" +
                    "     {\n" +
                    "       \"id\": 1,\n" +
                    "       \"name\": \"test item1\",\n" +
                    "       \"description\": \"test item 1 description\",\n" +
                    "       \"available\": true,\n" +
                    "       \"ownerId\": 20\n" +
                    "     },\n" +
                    "     {\n" +
                    "       \"id\": 2,\n" +
                    "       \"name\": \"test item2\",\n" +
                    "       \"description\": \"test item 2 description\",\n" +
                    "       \"available\": true,\n" +
                    "       \"ownerId\": 20,\n" +
                    "       \"lastBooking\": {\n" +
                    "               \"id\": 200,\n" +
                    "               \"bookerId\": 222\n" +
                    "           }\n" +
                    "       }\n" +
                        "]\n" +
                    "}";

        ItemRequestResponseDto dto = json.parse(jsonContent).getObject();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getDescription()).isEqualTo("Test description");
        assertThat(dto.getRequesterId()).isEqualTo(10L);
        assertThat(dto.getCreated())
                .isEqualTo(LocalDateTime.of(2026, 2, 10, 21, 0, 0));
        assertThat(dto.getItems()).hasSize(2);

        ItemDto item1Dto = dto.getItems().getFirst();
        assertThat(item1Dto.getId()).isEqualTo(1L);
        assertThat(item1Dto.getName()).isEqualTo("test item1");
        assertThat(item1Dto.getDescription()).isEqualTo("test item 1 description");
        assertThat(item1Dto.getAvailable()).isTrue();
        assertThat(item1Dto.getOwnerId()).isEqualTo(20L);

        ItemDto item2Dto = dto.getItems().getLast();
        assertThat(item2Dto.getId()).isEqualTo(2L);
        assertThat(item2Dto.getName()).isEqualTo("test item2");
        assertThat(item2Dto.getDescription()).isEqualTo("test item 2 description");
        assertThat(item2Dto.getAvailable()).isTrue();
        assertThat(item2Dto.getOwnerId()).isEqualTo(20L);
        assertThat(item2Dto.getLastBooking()).isNotNull();
        assertThat(item2Dto.getLastBooking().getId()).isEqualTo(200L);
        assertThat(item2Dto.getLastBooking().getBookerId()).isEqualTo(222L);
    }
}
