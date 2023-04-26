package ru.yandex.practicum.de.kk91.easymoney.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.PollerSpec;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.scheduling.PollerMetadata;

@Configuration
@EnableIntegration
@IntegrationComponentScan
public class AppConfiguration {
//    @Bean(name = PollerMetadata.DEFAULT_POLLER)
//    public PollerSpec poller() {
//        return Pollers.fixedRate(10000);
//    }
}
