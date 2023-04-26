package ru.yandex.practicum.de.kk91.easymoney.config.loader;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;
import ru.yandex.practicum.de.kk91.easymoney.loader.LoaderService;

@Configuration
public class LoaderFlowConfig {
    @Bean
    public MessageChannel loaderChannel() {
        return MessageChannels.flux()
                .datatype(Command.class)
                .get();
    }

    @Bean
    public IntegrationFlow loaderFlow(LoaderService loaderService) {
        return IntegrationFlow.from(loaderChannel())
                .channel(c -> c.queue("loaderBuffer"))
                .delay("")
                .<Command>handle((p, h) -> loaderService.downloadCommandAttachment(p), e -> e.async(true))
                .nullChannel();
    }
}
