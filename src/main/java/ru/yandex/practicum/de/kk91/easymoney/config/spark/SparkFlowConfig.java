package ru.yandex.practicum.de.kk91.easymoney.config.spark;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.MessageChannels;
import org.springframework.messaging.MessageChannel;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;

@Configuration
public class SparkFlowConfig {
    @Bean
    public MessageChannel sparkChannel() {
        return MessageChannels.publishSubscribe()
                .datatype(Command.class)
                .get();
    }

    @Bean
    public IntegrationFlow sparkFlow() {
        return IntegrationFlow.from(sparkChannel())
                .channel(c -> c.queue("sparkBuffer"))
                .log("sparkFlow")
                .get();
    }
}
