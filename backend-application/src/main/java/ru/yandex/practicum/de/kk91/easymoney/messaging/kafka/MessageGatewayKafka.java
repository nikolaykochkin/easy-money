package ru.yandex.practicum.de.kk91.easymoney.messaging.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;
import ru.yandex.practicum.de.kk91.easymoney.messaging.MessageGateway;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageGatewayKafka implements MessageGateway {
    @Value("${easy-money.messaging.kafka.topic.received-telegram-messages}")
    private String receivedTelegramMessagesTopic;

    @Value("${easy-money.messaging.kafka.topic.new-commands}")
    private String newCommandsTopic;
    @Value("${easy-money.messaging.kafka.topic.error-commands}")
    private String errorCommandsTopic;
    @Value("${easy-money.messaging.kafka.topic.load-command-attachments}")
    private String loadCommandAttachmentsTopic;
    @Value("${easy-money.messaging.kafka.topic.loaded-command-attachments}")
    private String loadedCommandAttachmentsTopic;
    @Value("${easy-money.messaging.kafka.topic.invoice-parsing-commands}")
    private String invoiceParsingCommandsTopic;

    @Value("${easy-money.messaging.kafka.topic.spark-nlp}")
    private String sparkNlpTopic;
    @Value("${easy-money.messaging.kafka.topic.spark-voice}")
    private String sparkVoiceTopic;
    @Value("${easy-money.messaging.kafka.topic.spark-photo}")
    private String sparkPhotoTopic;

    private final KafkaTemplate<UUID, TelegramMessage> telegramMessageKafkaTemplate;
    private final KafkaTemplate<UUID, Command> commandKafkaTemplate;

    @Override
    public void receiveTelegramMessage(TelegramMessage message) {
        telegramMessageKafkaTemplate.send(receivedTelegramMessagesTopic, message.getUuid(), message);
    }

    @Override
    public void handleNewCommand(Command command) {
        commandKafkaTemplate.send(newCommandsTopic, command.getUuid(), command);
    }

    @Override
    public void handleErrorCommand(Command command) {
        commandKafkaTemplate.send(errorCommandsTopic, command.getUuid(), command);
    }

    @Override
    public void loadCommandAttachments(Command command) {
        commandKafkaTemplate.send(loadCommandAttachmentsTopic, command.getUuid(), command);
    }

    @Override
    public void loadedCommandAttachments(Command command) {
        commandKafkaTemplate.send(loadedCommandAttachmentsTopic, command.getUuid(), command);
    }

    @Override
    public void sparkNlpCommand(Command command) {
        commandKafkaTemplate.send(sparkNlpTopic, command.getUuid(), command);
    }

    @Override
    public void sparkVoiceCommand(Command command) {
        commandKafkaTemplate.send(sparkVoiceTopic, command.getUuid(), command);
    }

    @Override
    public void sparkPhotoCommand(Command command) {
        commandKafkaTemplate.send(sparkPhotoTopic, command.getUuid(), command);
    }

    @Override
    public void handleInvoiceParsingCommand(Command command) {
        commandKafkaTemplate.send(invoiceParsingCommandsTopic, command.getUuid(), command);
    }
}
