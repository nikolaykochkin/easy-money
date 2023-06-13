package ru.yandex.practicum.de.kk91.easymoney.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.de.kk91.easymoney.bot.TelegramBotService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandFactory;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessageService;
import ru.yandex.practicum.de.kk91.easymoney.data.user.User;
import ru.yandex.practicum.de.kk91.easymoney.data.user.UserService;
import ru.yandex.practicum.de.kk91.easymoney.messaging.MessageGateway;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramMessageProcessorKafka {
    @Value("${easy-money.messaging.send-debug-messages}")
    private Boolean sendDebug;
    private final UserService userService;
    private final MessageGateway messageGateway;
    private final TelegramBotService telegramBotService;
    private final TelegramMessageService telegramMessageService;

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.received-telegram-messages}")
    public void processReceivedTelegramMessage(TelegramMessage message) {
        Optional<User> user = authTelegramUser(message);
        if (user.isPresent()) {
            message.setUser(user.get());
            TelegramMessage savedTelegramMessage = telegramMessageService.saveTelegramMessage(message);
            if (sendDebug) {
                telegramBotService.sendTelegramMessage(savedTelegramMessage, "Has been saved.");
            }
            messageGateway.handleNewCommand(CommandFactory.fromTelegramMessage(savedTelegramMessage));
        }
    }

    private Optional<User> authTelegramUser(TelegramMessage message) {
        if (Objects.isNull(message.getTgUserId())) {
            log.error("Couldn't process telegram message. tgUserId is null. {}", message);
            telegramBotService.sendErrorReply(message, "Telegram user id is empty.");
            return Optional.empty();
        }

        Optional<User> optionalUser = userService.getUserByTelegramId(message.getTgUserId());
        if (optionalUser.isEmpty()) {
            log.error("Couldn't process telegram message. User with telegramId '{}' not found. {}",
                    message.getTgUserId(), message);
            telegramBotService.sendErrorReply(message, "User not found.");
        }

        return optionalUser;
    }
}
