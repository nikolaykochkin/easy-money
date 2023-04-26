package ru.yandex.practicum.de.kk91.easymoney.config.command;

import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;

@MessagingGateway
public interface CommandGateway {
    @Gateway(requestChannel = "commandChannel")
    Mono<Void> handleCommand(Command command);
}
