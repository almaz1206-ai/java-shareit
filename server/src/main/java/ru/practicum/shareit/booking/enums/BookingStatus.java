package ru.practicum.shareit.booking.enums;


import lombok.Getter;

@Getter
public enum BookingStatus {
    WAITING(0),
    APPROVED(1),
    REJECTED(2),
    CANCELLED(3);

    private final int id;

    BookingStatus(int id) {
        this.id = id;
    }
}
