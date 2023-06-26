package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

public record TelegramFileMetadata(
        String fileId,
        String fileUniqueId,
        Long fileSize,
        String filePath
) {
}

