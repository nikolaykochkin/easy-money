package ru.yandex.practicum.de.kk91.easymoney.data.telegram;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "telegram_messages", schema = "stage")
public class TelegramMessage {
    private static final String ID_FIELD = "id";
    private static final String UUID_FIELD = "uuid";
    private static final String UPDATE_ID_FIELD = "update_id";
    private static final String MESSAGE_ID_FIELD = "message_id";
    private static final String CHAT_ID_FIELD = "chat_id";
    private static final String USER_ID_FIELD = "user_id";
    private static final String HAS_ATTACHMENT_FIELD = "has_attachment";
    private static final String CONTENT_FIELD = "content";

    @Id
    @JsonProperty(ID_FIELD)
    private Long id;

    @Column(UUID_FIELD)
    @JsonProperty(UUID_FIELD)
    private UUID uuid;

    @Column(UPDATE_ID_FIELD)
    @JsonProperty(UPDATE_ID_FIELD)
    private Integer updateId;

    @Column(MESSAGE_ID_FIELD)
    @JsonProperty(MESSAGE_ID_FIELD)
    private Integer messageId;

    @Column(CHAT_ID_FIELD)
    @JsonProperty(CHAT_ID_FIELD)
    private Long chatId;

    @Column(USER_ID_FIELD)
    @JsonProperty(USER_ID_FIELD)
    private Long userId;

    @Column(HAS_ATTACHMENT_FIELD)
    @JsonProperty(HAS_ATTACHMENT_FIELD)
    private Boolean hasAttachment;

    @Column(CONTENT_FIELD)
    @JsonProperty(CONTENT_FIELD)
    private String content;

    @Transient
    private Update update;

    public String getContent() {
        if (content == null) {
            if (update == null) {
                throw new IllegalStateException("Couldn't get content for Telegram Message: content and update is null.");
            }
            content = updateToJsonString(update);
        }
        return content;
    }

    public Update getUpdate() {
        if (update == null) {
            if (content == null) {
                throw new IllegalStateException("Couldn't get update for Telegram Message: content and update is null.");
            }
            update = jsonStringToUpdate(content);
        }
        return update;
    }

    public void setUpdate(Update update) {
        if (content == null) {
            content = updateToJsonString(update);
        }
        this.update = update;
    }

    public static String updateToJsonString(Update update) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(update);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static Update jsonStringToUpdate(String content) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(content, Update.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public SendMessage toSendMessage() {
        String text = String.format("Message saved with id '%s' and UUID '%s'", id.toString(), uuid.toString());
        return SendMessage.builder()
                .chatId(chatId)
                .replyToMessageId(messageId)
                .text(text)
                .build();
    }
}