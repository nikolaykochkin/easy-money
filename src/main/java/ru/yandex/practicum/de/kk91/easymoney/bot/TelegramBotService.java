package ru.yandex.practicum.de.kk91.easymoney.bot;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.de.kk91.easymoney.config.telegram.TelegramBotGateway;

import java.time.Duration;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {

    private final String username;

    private final TelegramBotGateway telegramBotGateway;

    public TelegramBotService(@Value("${bot.telegram-token}") String token,
                              @Value("${bot.username}") String username,
                              TelegramBotGateway telegramBotGateway) {
        super(token);
        this.username = username;
        this.telegramBotGateway = telegramBotGateway;
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
            telegramBotGateway.receiveTelegramUpdate(update)
                    .subscribe();
        }
    }

    public void sendMessage(SendMessage message) {
        log.debug("Received send message: {}.", message);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Couldn't send message: {}.", message, e);
            throw new RuntimeException(e);
        }
    }

    public Mono<String> getFileUrl(String fileId) {
        try {
            return Mono.fromFuture(executeAsync(new GetFile(fileId)))
                    .map(file -> File.getFileUrl(getBotToken(), file.getFilePath()));
        } catch (TelegramApiException e) {
            log.error("Couldn't get file URL by id: {}.", fileId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return username;
    }
}

