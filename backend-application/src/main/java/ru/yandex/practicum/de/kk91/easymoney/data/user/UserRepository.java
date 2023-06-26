package ru.yandex.practicum.de.kk91.easymoney.data.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByLogin(String login);

    Optional<User> findUserByTelegramId(Long telegramId);
}
