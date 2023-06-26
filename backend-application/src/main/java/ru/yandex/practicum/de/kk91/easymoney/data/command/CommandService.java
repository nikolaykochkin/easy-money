package ru.yandex.practicum.de.kk91.easymoney.data.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.repository.CommandRepository;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository commandRepository;

    @Transactional
    public Command saveCommand(Command command) {
        commandRepository.getCommandIdByUuid(command.getUuid())
                .ifPresent(command::setId);
        // The JSON serializer ignores the command field inside the CommandAttachment.
        // To avoid an error when saving, the command field is explicitly filled in for all attachments before saving.
        if (command.hasAttachments()) {
            command.getAttachments().forEach(attachment -> attachment.setCommand(command));
        }
        return commandRepository.save(command);
    }

    public Optional<Command> findCommandByUuid(UUID uuid) {
        return commandRepository.findCommandByUuid(uuid);
    }

}
