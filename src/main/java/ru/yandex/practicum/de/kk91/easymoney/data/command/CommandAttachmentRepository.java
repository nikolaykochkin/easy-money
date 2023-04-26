package ru.yandex.practicum.de.kk91.easymoney.data.command;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CommandAttachmentRepository extends ReactiveCrudRepository<CommandAttachment, Long> {
}
