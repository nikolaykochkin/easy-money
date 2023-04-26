package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;

@Service
public class TelegramMessageService {
    private final TelegramMessageRepository telegramMessageRepository;

    public TelegramMessageService(TelegramMessageRepository telegramMessageRepository) {
        this.telegramMessageRepository = telegramMessageRepository;
    }

    public Mono<TelegramMessage> saveTelegramMessage(TelegramMessage message) {
        return telegramMessageRepository.save(message);
    }

    public Mono<SendMessage> fromCommand(Command command) {
        if (command.getUuid() == null) {
            return null;
        }
        String text = switch (command.getState()) {
            case NEW -> String.format("Command saved with id '%s' and UUID '%s'",
                    command.getId(), command.getUuid());
            case ERROR -> String.format("ERROR command id '%s' and UUID '%s' error '%s'.",
                    command.getId(), command.getUuid(), command.getContent());
            default -> String.format("Command id '%s' and UUID '%s' content: '%s'",
                    command.getId(), command.getUuid(), command.getContent());
        };
        return telegramMessageRepository.findTelegramMessageByUuid(command.getUuid())
                .map(TelegramMessage::toSendMessage)
                .doOnSuccess(sendMessage -> sendMessage.setText(text));
    }
}
