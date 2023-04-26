package ru.yandex.practicum.de.kk91.easymoney.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.de.kk91.easymoney.data.command.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository commandRepository;
    private final CommandAttachmentRepository commandAttachmentRepository;

    public Mono<Command> saveCommandAsync(Command command) {
        if (command.getAttachment() != null) {
            return commandAttachmentRepository.save(command.getAttachment())
                    .doOnSuccess(command::setAttachment)
                    .flatMap(attachment -> commandRepository.save(command))
                    .doOnSuccess(c -> c.setAttachment(command.getAttachment()));
        } else {
            return commandRepository.save(command);
        }
    }

    public Mono<Command> newCommandHandler(Command command) {
        if (!CommandState.NEW.equals(command.getState())) {
            command.setState(CommandState.ERROR);
            command.setContent("New command handler handle only commands in state NEW. Original content: '"
                    + command.getContent() + "'");
        } else {
            if (command.getAttachment() != null) {
                command.setState(CommandState.LOADING);
            } else {
                if (CommandType.TEXT.equals(command.getType())) {
                    command.setState(CommandState.NLP);
                } else {
                    command.setState(CommandState.ERROR);
                    command.setContent("Illegal command type in NEW state. Original content: '"
                            + command.getContent() + "'");
                }
            }
        }
        return saveCommandAsync(command);
    }
}
