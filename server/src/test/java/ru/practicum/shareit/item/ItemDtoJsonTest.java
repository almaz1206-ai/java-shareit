package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemDtoJsonTest {
    private final JacksonTester<ItemDto> json;

    @Test
    void testSerialize() throws Exception {
        BookingDto lastBooking = BookingDto.builder()
                .id(1L)
                .bookerId(2L)
                .build();

        BookingDto nextBooking = BookingDto.builder()
                .id(2L)
                .bookerId(3L)
                .build();

        CommentDto comment1 = CommentDto.builder()
                .id(1L)
                .text("Хорошая вещь")
                .authorName("Test user")
                .itemId(1L)
                .created(LocalDateTime.now())
                .build();
        CommentDto comment2 = CommentDto.builder()
                .id(2L)
                .text("В хорошем состоянии")
                .authorName("Jane Smith")
                .itemId(1L)
                .created(LocalDateTime.now().minusDays(1))
                .build();

        List<CommentDto> comments = List.of(comment1, comment2);

        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Test Item")
                .description("Test Description")
                .available(true)
                .ownerId(10L)
                .requestId(5L)
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).hasJsonPath("$.ownerId");
        assertThat(result).hasJsonPath("$.requestId");
        assertThat(result).hasJsonPath("$.lastBooking");
        assertThat(result).hasJsonPath("$.nextBooking");
        assertThat(result).hasJsonPath("$.comments");

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(dto.getId().intValue());
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(dto.getName());
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(dto.getDescription());
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(dto.getAvailable());
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(dto.getOwnerId().intValue());
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(dto.getRequestId().intValue());

        assertThat(result).hasJsonPath("$.lastBooking.id");
        assertThat(result).hasJsonPath("$.lastBooking.bookerId");
        assertThat(result).hasJsonPath("$.nextBooking.id");
        assertThat(result).hasJsonPath("$.nextBooking.bookerId");

        assertThat(result).hasJsonPath("$.comments[0]");
        assertThat(result).hasJsonPath("$.comments[0].id");
        assertThat(result).hasJsonPath("$.comments[0].text");
        assertThat(result).hasJsonPath("$.comments[0].authorName");
        assertThat(result).hasJsonPath("$.comments[0].created");
    }

    @Test
    void testSerializeWithoutOptionalFields() throws Exception {
        ItemDto dto = ItemDto.builder()
                .id(1L)
                .name("Test item")
                .description("Test item description")
                .available(false)
                .ownerId(10L)
                .build();

        JsonContent<ItemDto> result = json.write(dto);

        assertThat(result).hasJsonPath("$.id");
        assertThat(result).hasJsonPath("$.name");
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).hasJsonPath("$.available");
        assertThat(result).hasJsonPath("$.ownerId");

        assertThat(result).extractingJsonPathNumberValue("$.requestId").isNull();
        assertThat(result).extractingJsonPathValue("$.lastBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.nextBooking").isNull();
        assertThat(result).extractingJsonPathValue("$.comments").isNull();
    }

    @Test
    void testDeserialize() throws Exception {
        String jsonContent = "{\n" +
            "       \"id\": 1,\n" +
            "       \"name\": \"Test item\",\n" +
            "       \"description\": \"Test description\",\n" +
            "       \"available\": true,\n" +
            "       \"ownerId\": 10,\n" +
            "       \"requestId\": 5\n" +
            "   }";

        ItemDto dto = json.parse(jsonContent).getObject();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Test item");
        assertThat(dto.getDescription()).isEqualTo("Test description");
        assertThat(dto.getAvailable()).isTrue();
        assertThat(dto.getOwnerId()).isEqualTo(10L);
        assertThat(dto.getRequestId()).isEqualTo(5L);
    }
}
