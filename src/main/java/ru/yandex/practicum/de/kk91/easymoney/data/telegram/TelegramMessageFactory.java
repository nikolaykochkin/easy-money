package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;
import java.util.UUID;

public class TelegramMessageFactory {
    public static TelegramMessage fromUpdate(Update update, UUID uuid) {
        Message message = update.getMessage();
        return TelegramMessage.builder()
                .uuid(Optional.ofNullable(uuid).orElseGet(UUID::randomUUID))
                .updateId(update.getUpdateId())
                .messageId(message.getMessageId())
                .chatId(message.getChatId())
                .userId(message.getFrom().getId())
                .hasAttachment(message.hasPhoto() || message.hasVoice())
                .update(update)
                .content(TelegramMessage.updateToJsonString(update))
                .build();
    }
}
