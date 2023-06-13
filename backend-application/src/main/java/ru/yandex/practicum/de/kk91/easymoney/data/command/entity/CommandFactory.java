package ru.yandex.practicum.de.kk91.easymoney.data.command.entity;

import org.springframework.util.StringUtils;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramFileMetadata;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;

import java.util.Collections;

public class CommandFactory {
    public static Command fromTelegramMessage(TelegramMessage message) {
        Command command = Command.builder()
                .uuid(message.getUuid())
                .telegramMessage(message)
                .source(CommandSource.TELEGRAM_MESSAGE)
                .state(CommandState.NEW)
                .user(message.getUser())
                .build();

        if (StringUtils.hasText(message.getPhotoFilePath())) {
            CommandAttachment attachment = attachmentFromTelegramMessage(command, message.getPhotoFilePath());
            command.setType(CommandType.PHOTO);
            command.setAttachments(Collections.singletonList(attachment));
        } else if (StringUtils.hasText(message.getVoiceFilePath())) {
            CommandAttachment attachment = attachmentFromTelegramMessage(command, message.getVoiceFilePath());
            command.setType(CommandType.VOICE);
            command.setAttachments(Collections.singletonList(attachment));
        } else if (StringUtils.hasText(message.getText())) {
            command.setType(CommandType.TEXT);
            command.setContent(message.getText());
        } else {
            command.setType(CommandType.TEXT);
            command.setState(CommandState.ERROR);
            command.setError("Couldn't handle command, unknown telegram message type.");
        }

        return command;
    }

    private static CommandAttachment attachmentFromTelegramMessage(Command command, String fileUrl) {
        TelegramFileMetadata fileMetadata = command.getTelegramMessage().getFileMetadata();
        return CommandAttachment.builder()
                .uuid(command.getUuid())
                .url(fileUrl)
                .telegramId(fileMetadata.fileId())
                .filesize(fileMetadata.fileSize())
                .filename(fileMetadata.filePath())
                .command(command)
                .build();
    }
}
