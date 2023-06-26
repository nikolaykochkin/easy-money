package ru.yandex.practicum.de.kk91.easymoney.data.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandAttachment;

public interface CommandAttachmentRepository extends JpaRepository<CommandAttachment, Long> {
}
