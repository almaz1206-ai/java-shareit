package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    @Query("SELECT item FROM Item item " +
           "WHERE item.available = TRUE and (LOWER(item.name)) like LOWER(concat('%', ?1, '%')) " +
            "OR LOWER(item.description) LIKE LOWER(concat('%', ?1, '%'))")
    List<Item> searchAvailableItems(String text);
}
