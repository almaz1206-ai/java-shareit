package ru.practicum.shareit.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.user.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE LOWER(u.email) = LOWER(:email) " +
            "AND (:userId IS NULL OR u.id != :userId)")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);
}
