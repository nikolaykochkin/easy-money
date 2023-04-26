package ru.yandex.practicum.de.kk91.easymoney.config.command;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import ru.yandex.practicum.de.kk91.easymoney.command.CommandService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandFactory;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandState;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;

@Configuration
public class CommandFlowConfig {
    @Bean
    public MessageChannel commandChannel() {
        return MessageChannels.flux()
                .datatype(Command.class)
                .get();
    }

    @Bean
    public IntegrationFlow handleTelegramMessage(MessageChannel savedTelegramMessagesChannel,
                                                 CommandService commandService,
                                                 MessageChannel sendCommandChannel) {
        return IntegrationFlow.from(savedTelegramMessagesChannel)
                .transform(TelegramMessage.class, CommandFactory::fromTelegramMessage)
                .<Command>handle((p, h) -> commandService.saveCommandAsync(p), e -> e.async(true))
                .publishSubscribeChannel(c -> c
                        .subscribe(sf -> sf.channel(commandChannel()))
                        .subscribe(sf -> sf.channel(sendCommandChannel))
                )
                .nullChannel();
    }

    @Bean
    public IntegrationFlow commandFlow(CommandService commandService,
                                       MessageChannel loaderChannel,
                                       MessageChannel sparkChannel,
                                       MessageChannel sendCommandChannel) {
        return IntegrationFlow.from(commandChannel())
                .<Command, CommandState>route(Command::getState, mapping -> mapping
                        .subFlowMapping(CommandState.NEW, sf -> sf
                                .<Command>handle((p, h) -> commandService.newCommandHandler(p), e -> e.async(true))
                                .channel(commandChannel()))
                        .channelMapping(CommandState.LOADING, loaderChannel)
                        .subFlowMapping(CommandState.LOADED, sf -> sf
                                .<Command>handle((p, h) -> commandService.saveCommandAsync(p), e -> e.async(true))
                                .channel(sparkChannel))
                        .channelMapping(CommandState.NLP, sparkChannel)
                        .channelMapping(CommandState.IMAGE_PROCESSING, sparkChannel)
                        .channelMapping(CommandState.VOICE_PROCESSING, sparkChannel)
                        .channelMapping(CommandState.ERROR, sendCommandChannel)
                )
                .get();
    }

}
