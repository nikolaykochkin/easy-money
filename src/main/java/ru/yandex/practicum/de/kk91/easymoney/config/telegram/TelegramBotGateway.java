package ru.yandex.practicum.de.kk91.easymoney.config.telegram;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.handler.annotation.Payload;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;

@MessagingGateway
public interface TelegramBotGateway {
    @Gateway(requestChannel = "receiveTelegramUpdateChannel")
    Mono<Void> receiveTelegramUpdate(@Payload Update update);

    @Gateway(requestChannel = "getFileUrlChannel")
    Mono<String> getFileUrl(@Payload String fileId);
}
