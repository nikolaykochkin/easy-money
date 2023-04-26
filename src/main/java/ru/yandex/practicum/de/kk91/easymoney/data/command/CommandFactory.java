package ru.yandex.practicum.de.kk91.easymoney.data.command;

import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CommandFactory {
    private static final String DEFAULT_TG_PHOTO_MIME_TYPE = "image/jpeg";

    public static Command fromTelegramMessage(TelegramMessage message) {
        Command command = Command.builder()
                .uuid(message.getUuid())
                .source(CommandSource.TELEGRAM_MESSAGE)
                .state(CommandState.NEW)
                .build();

        Update update = message.getUpdate();
        if (update.getMessage().hasText()) {
            command.setType(CommandType.TEXT);
            command.setContent(update.getMessage().getText());
        } else if (update.getMessage().hasVoice()) {
            CommandAttachment attachment = fromVoice(update.getMessage().getVoice());
            attachment.setUuid(message.getUuid());
            command.setType(CommandType.VOICE);
            command.setAttachment(attachment);
        } else if (update.getMessage().hasPhoto()) {
            command.setType(CommandType.PHOTO);
            Optional<CommandAttachment> optional = fromPhoto(update.getMessage().getPhoto());
            optional.ifPresentOrElse(
                    attachment -> {
                        attachment.setUuid(message.getUuid());
                        command.setAttachment(attachment);
                    },
                    () -> {
                        command.setState(CommandState.ERROR);
                        command.setContent("Couldn't get attachment from telegram message photo.");
                    }
            );
        } else {
            command.setType(CommandType.TEXT);
            command.setState(CommandState.ERROR);
            command.setContent("Couldn't handle command, unknown telegram message type.");
        }

        return command;
    }

    public static CommandAttachment fromVoice(Voice voice) {
        return CommandAttachment.builder()
                .url(voice.getFileId())
                .filesize(voice.getFileSize())
                .mimeType(voice.getMimeType())
                .build();
    }

    public static Optional<CommandAttachment> fromPhoto(List<PhotoSize> photo) {
        return photo.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(photoSize -> CommandAttachment.builder()
                        .url(photoSize.getFileId())
                        .filesize(Long.valueOf(photoSize.getFileSize()))
                        .mimeType(DEFAULT_TG_PHOTO_MIME_TYPE)
                        .build());
    }
}
