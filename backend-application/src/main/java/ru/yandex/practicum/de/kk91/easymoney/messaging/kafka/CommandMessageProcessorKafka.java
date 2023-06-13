package ru.yandex.practicum.de.kk91.easymoney.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.de.kk91.easymoney.bot.TelegramBotService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandState;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandType;
import ru.yandex.practicum.de.kk91.easymoney.messaging.MessageGateway;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandMessageProcessorKafka {
    @Value("${easy-money.messaging.send-debug-messages}")
    private Boolean sendDebug;

    private final CommandService commandService;
    private final MessageGateway messageGateway;
    private final TelegramBotService telegramBotService;

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.new-commands}")
    public void processNewCommand(Command command) {
        Command savedCommand = commandService.saveCommand(command);

        if (CommandState.ERROR.equals(savedCommand.getState())) {
            telegramBotService.sendCommand(savedCommand);
            return;
        }

        if (sendDebug) {
            telegramBotService.sendCommand(savedCommand);
        }

        if (savedCommand.hasAttachments()) {
            savedCommand.setState(CommandState.LOADING);
            commandService.saveCommand(savedCommand);
            messageGateway.loadCommandAttachments(savedCommand);
            return;
        }

        if (StringUtils.hasText(command.getContent())) {
            savedCommand.setState(CommandState.NLP);
            commandService.saveCommand(savedCommand);
            messageGateway.sparkNlpCommand(command);
            return;
        }

        log.error("Couldn't process new command. Command has unknown content. {}", savedCommand);
        savedCommand.setError("Command has unknown content.");
        processErrorCommand(savedCommand);
    }

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.loaded-command-attachments}")
    public void processLoadedCommand(Command command) {
        if (CommandType.VOICE.equals(command.getType())) {
            command.setState(CommandState.VOICE_PROCESSING);
            Command savedCommand = commandService.saveCommand(command);
            messageGateway.sparkVoiceCommand(savedCommand);
            return;
        }

        if (CommandType.PHOTO.equals(command.getType())) {
            command.setState(CommandState.IMAGE_PROCESSING);
            Command savedCommand = commandService.saveCommand(command);
            messageGateway.sparkPhotoCommand(savedCommand);
            return;
        }

        log.error("Couldn't process loaded command. Command has unknown type. {}", command);
        command.setError("Command has unknown type" + command.getType());
        processErrorCommand(command);
    }

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.error-commands}")
    public void processErrorCommand(Command command) {
        command.setState(CommandState.ERROR);
        Command savedCommand = commandService.saveCommand(command);
        telegramBotService.sendCommand(savedCommand);
    }
}
