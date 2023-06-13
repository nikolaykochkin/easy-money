package ru.yandex.practicum.de.kk91.easymoney.messaging;

import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;

public interface MessageGateway {
    void receiveTelegramMessage(TelegramMessage message);

    void handleNewCommand(Command command);
    void handleErrorCommand(Command command);

    void loadCommandAttachments(Command command);

    void loadedCommandAttachments(Command command);

    void sparkNlpCommand(Command command);

    void sparkVoiceCommand(Command command);

    void sparkPhotoCommand(Command command);
}
