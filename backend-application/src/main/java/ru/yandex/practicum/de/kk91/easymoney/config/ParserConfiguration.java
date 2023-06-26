package ru.yandex.practicum.de.kk91.easymoney.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(ParserPropertiesMontenegro.class)
public class ParserConfiguration {

    private final ParserPropertiesMontenegro parserPropertiesMontenegro;

    public ParserConfiguration(ParserPropertiesMontenegro parserPropertiesMontenegro) {
        this.parserPropertiesMontenegro = parserPropertiesMontenegro;
    }

    @Bean
    public ReactorClientHttpConnector montenegroHttpConnector() {
        Long timeout = parserPropertiesMontenegro.getTimeout();
        HttpClient httpClient = HttpClient.create()
                .wiretap(true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout.intValue())
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));
        return new ReactorClientHttpConnector(httpClient);
    }

    @Bean
    public WebClient webClientMontenegro(ReactorClientHttpConnector montenegroHttpConnector) {
        return WebClient.builder()
                .baseUrl(parserPropertiesMontenegro.getParseUrl())
                .defaultHeaders(httpHeaders -> httpHeaders.setAll(parserPropertiesMontenegro.getHeaders()))
                .clientConnector(montenegroHttpConnector)
                .build();
    }
}
