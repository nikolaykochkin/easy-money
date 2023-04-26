package ru.yandex.practicum.de.kk91.easymoney.data.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "commands", schema = "command")
public class Command {
    private static final String ID_FIELD = "id";
    private static final String UUID_FIELD = "uuid";
    private static final String SOURCE_FIELD = "source";
    private static final String STATE_FIELD = "state";
    private static final String TYPE_FIELD = "type";
    private static final String CONTENT_FIELD = "content";
    private static final String SQL_FIELD = "sql";

    @Id
    @Column(ID_FIELD)
    @JsonProperty(ID_FIELD)
    private Long id;

    @Column(UUID_FIELD)
    @JsonProperty(UUID_FIELD)
    private UUID uuid;

    @Column(SOURCE_FIELD)
    @JsonProperty(SOURCE_FIELD)
    private CommandSource source;

    @Column(STATE_FIELD)
    @JsonProperty(STATE_FIELD)
    private CommandState state;

    @Column(TYPE_FIELD)
    @JsonProperty(TYPE_FIELD)
    private CommandType type;

    @Column(CONTENT_FIELD)
    @JsonProperty(CONTENT_FIELD)
    private String content;

    @Column(SQL_FIELD)
    @JsonProperty(SQL_FIELD)
    private String sql;

    @Transient
    private CommandAttachment attachment;
}
