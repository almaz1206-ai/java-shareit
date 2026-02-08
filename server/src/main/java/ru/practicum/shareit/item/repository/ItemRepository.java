package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findAllByOwnerId(Long ownerId);

    @Query("SELECT item FROM Item item " +
           "WHERE item.available = TRUE and (LOWER(item.name)) like LOWER(concat('%', ?1, '%')) " +
            "OR LOWER(item.description) LIKE LOWER(concat('%', ?1, '%'))")
    List<Item> searchAvailableItems(String text);

    List<Item> findByRequestId(Long requestId);

    @Query("SELECT i FROM Item i WHERE i.request.id IN :requestIds")
    List<Item> findByRequestIdIn(@Param("requestIds") List<Long> requestIds);
}
