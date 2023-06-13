package ru.yandex.practicum.de.kk91.easymoney.bot;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandSource;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessageFactory;
import ru.yandex.practicum.de.kk91.easymoney.messaging.MessageGateway;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final String username;
    private final String downloadPath;
    private final String sep = java.io.File.separator;

    private final MessageGateway messagingService;

    public TelegramBotService(@Value("${easy-money.bot.telegram-token}") String token,
                              @Value("${easy-money.bot.username}") String username,
                              @Value("${easy-money.bot.download-path}") String downloadPath,
                              MessageGateway messagingService) {
        super(token);
        this.username = username;
        this.downloadPath = downloadPath;
        this.messagingService = messagingService;
    }

    @PostConstruct
    private void initialize() {
        log.info("Start initializing bot...");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this);
        } catch (TelegramApiException e) {
            log.error("Something went wrong while initializing bot.", e);
        }
        log.info("Bot is ready for use...");
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.debug("Received update: {}", update);
        if (update.hasMessage()) {
            Message message = update.getMessage();
            TelegramMessage telegramMessage = TelegramMessageFactory.fromUpdate(update);
            if (StringUtils.hasText(telegramMessage.getUpdate())) {
                if (message.hasText()) {
                    handleTextMessage(telegramMessage, message);
                } else if (message.hasVoice()) {
                    handleVoiceMessage(telegramMessage, message);
                } else if (message.hasPhoto()) {
                    handlePhotoMessage(telegramMessage, message);
                } else {
                    handleUnknownContent(telegramMessage);
                }
            } else {
                handleEmptyUpdate(update);
            }
        }
    }

    private void handlePhotoMessage(TelegramMessage telegramMessage, Message message) {
        message.getPhoto().stream()
                .max(Comparator.comparing(PhotoSize::getFileSize))
                .map(
                        photoSize -> {
                            try {
                                return execute(new GetFile(photoSize.getFileId()));
                            } catch (TelegramApiException e) {
                                log.error("Couldn't get path for fileId: {}. {}", photoSize.getFileId(), e.getMessage(), e);
                                return null;
                            }
                        }
                )
                .map(
                        file -> {
                            telegramMessage.setFileMetadata(TelegramMessageFactory.getFileMetadata(file));
                            return downloadSync(file, telegramMessage.getUuid().toString());
                        }
                )
                .ifPresentOrElse(
                        file -> {
                            telegramMessage.setPhotoFilePath(file.toURI().toString());
                            messagingService.receiveTelegramMessage(telegramMessage);
                        },
                        () -> sendErrorReply(telegramMessage, "Couldn't get photo from telegram")
                );
    }


    private void handleVoiceMessage(TelegramMessage telegramMessage, Message message) {
        Optional.ofNullable(message.getVoice())
                .map(
                        voice -> {
                            try {
                                return execute(new GetFile(voice.getFileId()));
                            } catch (TelegramApiException e) {
                                log.error("Couldn't get path for fileId: {}. {}", voice.getFileId(), e.getMessage(), e);
                                return null;
                            }
                        }
                )
                .map(
                        file -> {
                            telegramMessage.setFileMetadata(TelegramMessageFactory.getFileMetadata(file));
                            return downloadSync(file, telegramMessage.getUuid().toString());
                        }
                )
                .ifPresentOrElse(
                        file -> {
                            telegramMessage.setVoiceFilePath(file.toURI().toString());
                            messagingService.receiveTelegramMessage(telegramMessage);
                        },
                        () -> sendErrorReply(telegramMessage, "Couldn't get voice from telegram")
                );
    }

    private void handleTextMessage(TelegramMessage telegramMessage, Message message) {
        telegramMessage.setText(message.getText());
        messagingService.receiveTelegramMessage(telegramMessage);
    }


    public void sendMessage(SendMessage message) {
        log.debug("Sending message: {}.", message);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Couldn't send message: {}.", message, e);
        }
    }

    public void sendTelegramMessage(TelegramMessage telegramMessage, String message) {
        String text = String.format("TelegramMessage(id='%s', uuid='%s')%n%s", telegramMessage.getId(),
                telegramMessage.getUuid(), message);
        sendMessage(getReplyMessage(telegramMessage, text));
    }

    public void sendCommand(Command command) {
        if (CommandSource.TELEGRAM_MESSAGE.equals(command.getSource())
                && Objects.nonNull(command.getTelegramMessage())) {
            sendMessage(getReplyMessage(command.getTelegramMessage(), command.getUserMessage()));
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }


    private java.io.File downloadSync(File file, String uuid) {
        String path = downloadPath + sep + uuid + sep + file.getFilePath();
        java.io.File downloaded = new java.io.File(path);
        try {
            downloadFile(file, downloaded);
        } catch (TelegramApiException e) {
            log.error("Couldn't download file for filePath: {}. {}", file.getFilePath(), e.getMessage(), e);
            return null;
        }
        return downloaded;
    }

    private void handleUnknownContent(TelegramMessage message) {
        log.error("Unknown message content. {}", message.getUpdate());
        sendErrorReply(message, "Unknown message content");
    }

    private void handleEmptyUpdate(Update update) {
        log.error("Couldn't get JSON string from telegram update. {}", update);
        String message_text = "Something went wrong, while handling update." + System.lineSeparator() + update;
        sendMessage(
                SendMessage.builder()
                        .chatId(update.getMessage().getChatId())
                        .text(message_text)
                        .replyToMessageId(update.getMessage().getMessageId())
                        .build()
        );
    }

    public void sendErrorReply(TelegramMessage message, String error) {
        String text = String.format("Something went wrong.%nError: %s.%n%s",
                error, message.getUpdate());
        SendMessage replyMessage = getReplyMessage(message, text);
        sendMessage(replyMessage);
    }

    private SendMessage getReplyMessage(TelegramMessage message, String text) {
        return SendMessage.builder()
                .chatId(message.getChatId())
                .text(text)
                .replyToMessageId(message.getMessageId())
                .build();
    }

}

