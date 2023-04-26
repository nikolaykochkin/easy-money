package ru.yandex.practicum.de.kk91.easymoney.config.telegram;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.yandex.practicum.de.kk91.easymoney.bot.TelegramBotService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessageFactory;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessageService;

@Configuration
public class TelegramFlowConfig {
    @Bean
    public MessageChannel receiveTelegramUpdateChannel() {
        return MessageChannels.flux()
                .datatype(Update.class)
                .get();
    }

    @Bean
    public MessageChannel savedTelegramMessagesChannel() {
        return MessageChannels.flux()
                .datatype(TelegramMessage.class)
                .get();
    }

    @Bean
    public MessageChannel sendTelegramMessageChannel() {
        return MessageChannels.flux()
                .datatype(SendMessage.class)
                .get();
    }

    @Bean
    public MessageChannel sendCommandChannel() {
        return MessageChannels.flux()
                .datatype(Command.class)
                .get();
    }

    @Bean
    public MessageChannel getFileUrlChannel() {
        return MessageChannels.flux()
                .datatype(String.class)
                .get();
    }

    @Bean
    public IntegrationFlow sendTelegramMessage(TelegramBotService botService) {
        return IntegrationFlow.from(sendTelegramMessageChannel())
                .handle(m -> botService.sendMessage((SendMessage) m.getPayload()))
                .get();
    }

    @Bean
    public IntegrationFlow receiveTelegramUpdate(TelegramMessageService messageService) {
        return IntegrationFlow.from(receiveTelegramUpdateChannel())
                .transform(Message.class,
                        m -> TelegramMessageFactory.fromUpdate((Update) m.getPayload(), m.getHeaders().getId()))
                .handle(TelegramMessage.class, (p, h) -> messageService.saveTelegramMessage(p), e -> e.async(true))
                .publishSubscribeChannel(c -> c
                        .subscribe(sf -> sf.channel(savedTelegramMessagesChannel()))
                        .subscribe(sf -> sf
                                .transform(TelegramMessage.class, TelegramMessage::toSendMessage)
                                .channel(sendTelegramMessageChannel())
                        )
                ).nullChannel();
    }

    @Bean
    public IntegrationFlow sendCommand(TelegramMessageService messageService) {
        return IntegrationFlow.from(sendCommandChannel())
                .<Command>handle((p, h) -> messageService.fromCommand(p), e -> e.async(true))
                .channel(sendTelegramMessageChannel())
                .nullChannel();
    }

    @Bean
    public IntegrationFlow getFileUrl(TelegramBotService botService) {
        return IntegrationFlow.from(getFileUrlChannel())
                .<String>handle((p, h) -> botService.getFileUrl(p), e -> e.async(true))
                .get();
    }
}
