package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.enums.BookingStatus;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfter(
            Long bookerId, LocalDateTime startBefore, LocalDateTime endAfter, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfter(
            Long ownerId, LocalDateTime now1, LocalDateTime now2, Sort sort);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndItemIdAndStatusAndEndBefore(
            Long bookerId, Long itemId, BookingStatus status, LocalDateTime now);

    List<Booking> findByItemId(Long itemId);
}
