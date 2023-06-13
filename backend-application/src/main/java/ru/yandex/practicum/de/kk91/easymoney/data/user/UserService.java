package ru.yandex.practicum.de.kk91.easymoney.data.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<User> getUserByTelegramId(Long telegramId) {
        return userRepository.findUserByTelegramId(telegramId);
    }
}
