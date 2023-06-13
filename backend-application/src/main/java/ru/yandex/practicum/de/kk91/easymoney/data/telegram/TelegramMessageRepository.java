package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TelegramMessageRepository extends JpaRepository<TelegramMessage, Long> {
    Optional<TelegramMessage> findTelegramMessageByUuid(UUID uuid);
    @Query("SELECT e.id FROM #{#entityName} e WHERE e.uuid = :uuid")
    Optional<Long> getTelegramMessageIdByUuid(@Param("uuid") UUID uuid);
}
