package ru.yandex.practicum.de.kk91.easymoney.data.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramMessageService {
    private final TelegramMessageRepository telegramMessageRepository;

    public TelegramMessage saveTelegramMessage(TelegramMessage message) {
        telegramMessageRepository.getTelegramMessageIdByUuid(message.getUuid())
                .ifPresent(message::setId);
        return telegramMessageRepository.save(message);
    }

    public Optional<TelegramMessage> findTelegramMessageById(Long id) {
        return telegramMessageRepository.findById(id);
    }

    public Optional<TelegramMessage> findTelegramMessageByUuid(UUID uuid) {
        return telegramMessageRepository.findTelegramMessageByUuid(uuid);
    }
}
