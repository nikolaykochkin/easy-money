package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface TelegramMessageRepository extends ReactiveCrudRepository<TelegramMessage, Long> {
    Mono<TelegramMessage> findTelegramMessageByUuid(UUID uuid);
}
