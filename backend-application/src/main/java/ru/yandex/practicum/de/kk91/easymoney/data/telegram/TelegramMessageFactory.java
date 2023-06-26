package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public final class TelegramMessageFactory {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static TelegramMessage fromUpdate(Update update) {
        Message message = update.getMessage();
        return TelegramMessage.builder()
                .uuid(UUID.randomUUID())
                .messageDate(Instant.ofEpochSecond(message.getDate()))
                .updateId(update.getUpdateId())
                .messageId(message.getMessageId())
                .chatId(message.getChatId())
                .tgUserId(message.getFrom().getId())
                .text(message.getText())
                .update(getUpdateAsJsonString(update))
                .build();
    }

    public static String getUpdateAsJsonString(Update update) {
        try {
            return objectMapper.writeValueAsString(update);
        } catch (JsonProcessingException e) {
            log.error("Couldn't serialize update object to JSON string. {}", e.getMessage(), e);
            return null;
        }
    }

    public static Update jsonStringAsUpdate(String json) {
        try {
            return objectMapper.readValue(json, Update.class);
        } catch (JsonProcessingException e) {
            log.error("Couldn't deserialize JSON string to update object. {}", e.getMessage(), e);
            return null;
        }
    }

    public static TelegramFileMetadata getFileMetadata(File file) {
        return new TelegramFileMetadata(
                file.getFileId(),
                file.getFileUniqueId(),
                file.getFileSize(),
                file.getFilePath()
        );
    }

}
