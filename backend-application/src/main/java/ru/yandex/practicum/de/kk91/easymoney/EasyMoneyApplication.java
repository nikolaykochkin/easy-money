package ru.yandex.practicum.de.kk91.easymoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EasyMoneyApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyMoneyApplication.class, args);
    }
}
