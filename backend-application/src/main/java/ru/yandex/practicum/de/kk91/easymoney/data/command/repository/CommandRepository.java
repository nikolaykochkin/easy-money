package ru.yandex.practicum.de.kk91.easymoney.data.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;

import java.util.Optional;
import java.util.UUID;

public interface CommandRepository extends JpaRepository<Command, Long> {
    Optional<Command> findCommandByUuid(UUID uuid);

    @Query("SELECT e.id FROM #{#entityName} e WHERE e.uuid = :uuid")
    Optional<Long> getCommandIdByUuid(@Param("uuid") UUID uuid);
}
