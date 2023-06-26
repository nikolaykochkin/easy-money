package ru.yandex.practicum.de.kk91.easymoney.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "easy-money.parser.montenegro")
public class ParserPropertiesMontenegro {
    private String baseUrl;
    private String parseUrl;
    private Long timeout;
    private Map<String, String> headers;
}
