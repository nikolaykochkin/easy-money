package ru.yandex.practicum.de.kk91.easymoney.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandState;
import ru.yandex.practicum.de.kk91.easymoney.loader.LoaderService;
import ru.yandex.practicum.de.kk91.easymoney.messaging.MessageGateway;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoaderMessageProcessorKafka {
    private final LoaderService loaderService;
    private final MessageGateway messageGateway;

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.load-command-attachments}")
    public void processLoadCommandAttachments(Command command) {
        if (command.hasAttachments()) {
            try {
                loaderService.uploadCommandAttachments(command);
                messageGateway.loadedCommandAttachments(command);
            } catch (Exception e) {
                handleErrorCommand(command, "Couldn't upload command attachments. " + e.getMessage(), e);
            }
        } else {
            handleErrorCommand(command, "Loader got command without attachments.", null);
        }
    }

    private void handleErrorCommand(Command command, String error, Exception e) {
        log.error("{}. {}", error, command, e);
        command.setError(error);
        messageGateway.handleErrorCommand(command);
    }
}
