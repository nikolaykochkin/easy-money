package ru.yandex.practicum.de.kk91.easymoney.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.de.kk91.easymoney.bot.TelegramBotService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandService;
import ru.yandex.practicum.de.kk91.easymoney.data.command.dto.SparkCommandAttachmentDto;
import ru.yandex.practicum.de.kk91.easymoney.data.command.dto.SparkCommandDto;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandState;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandType;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.TransactionService;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Invoice;
import ru.yandex.practicum.de.kk91.easymoney.messaging.MessageGateway;
import ru.yandex.practicum.de.kk91.easymoney.parser.Parser;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandMessageProcessorKafka {
    @Value("${easy-money.messaging.send-debug-messages}")
    private Boolean sendDebug;

    private final CommandService commandService;
    private final MessageGateway messageGateway;
    private final TelegramBotService telegramBotService;
    private final TransactionService transactionService;
    private final Parser parser;

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
            if (StringUtils.startsWithIgnoreCase(command.getContent(), "http")) {
                savedCommand.setState(CommandState.INVOICE_PARSING);
                commandService.saveCommand(savedCommand);
                messageGateway.handleInvoiceParsingCommand(command);
            } else {
                savedCommand.setState(CommandState.NLP);
                commandService.saveCommand(savedCommand);
                messageGateway.sparkNlpCommand(command);
            }
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

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.spark-photo-decoded}")
    public void processDecodedCommand(SparkCommandDto sparkCommandDto) {
        Optional<Command> optionalCommand = commandService.findCommandByUuid(sparkCommandDto.getUuid());
        if (optionalCommand.isEmpty()) {
            log.error("Something went wrong while process decoded command with UUID: {} not found.", sparkCommandDto.getUuid());
            return;
        }
        Command existingCommand = optionalCommand.get();
        Map<Long, String> decodedData = sparkCommandDto.getAttachments().stream()
                .collect(Collectors.toMap(SparkCommandAttachmentDto::getId, SparkCommandAttachmentDto::getDecoded));

        existingCommand.getAttachments()
                .forEach(attachment -> attachment.setDecoded(decodedData.get(attachment.getId())));

        existingCommand.setState(CommandState.INVOICE_PARSING);

        Command savedCommand = commandService.saveCommand(existingCommand);

        messageGateway.handleInvoiceParsingCommand(savedCommand);
    }

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.invoice-parsing-commands}")
    public void processInvoiceParsingCommand(Command command) {
        Optional<String> url;
        if (command.hasAttachments()) {
            url = Optional.ofNullable(command.getAttachments().get(0).getDecoded());
        } else {
            url = Optional.ofNullable(command.getContent());
        }

        if (url.isEmpty()) {
            log.error("Couldn't parse invoice. Command has no invoice URL. {}", command);
            command.setError("Command has no invoice URL.");
            processErrorCommand(command);
            return;
        }

        Optional<Invoice> optionalInvoice = parser.parseInvoice(url.get());
        if (optionalInvoice.isEmpty()) {
            command.setError("Couldn't parse invoice from URL: " + url.get());
            processErrorCommand(command);
            return;
        }

        Invoice invoice = optionalInvoice.get();
        invoice.setUuid(command.getUuid());
        invoice.setUser(command.getUser());

        Invoice savedInvoice;
        try {
            savedInvoice = transactionService.saveParsedInvoice(invoice);
        } catch (Exception e) {
            log.error("Couldn't save invoice. Cause {}. {}", e.getMessage(), invoice, e);
            command.setError("Couldn't save invoice. Cause " + e.getMessage());
            return;
        }

        command.setContent("Invoice Has been saved.\n" + savedInvoice.getUserReport());
        command.setState(CommandState.DONE);

        Command savedCommand = commandService.saveCommand(command);

        telegramBotService.sendCommand(savedCommand);
    }

    @KafkaListener(topics = "${easy-money.messaging.kafka.topic.error-commands}")
    public void processErrorCommand(Command command) {
        command.setState(CommandState.ERROR);
        Command savedCommand = commandService.saveCommand(command);
        telegramBotService.sendCommand(savedCommand);
    }
}
