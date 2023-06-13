package ru.yandex.practicum.de.kk91.easymoney.data.command.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.yandex.practicum.de.kk91.easymoney.data.telegram.TelegramMessage;
import ru.yandex.practicum.de.kk91.easymoney.data.user.User;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "command")
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private UUID uuid;

    private CommandSource source;
    private CommandState state;
    private CommandType type;

    private String content;
    private String error;
    private String sql;

    @ManyToOne
    private TelegramMessage telegramMessage;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "command_id")
    private List<CommandAttachment> attachments;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    public String getUserMessage() {
        return switch (state) {
            case NEW -> String.format("Command(id='%s', uuid='%s')%nHas been saved.", id, uuid);
            case ERROR -> String.format("Command(id='%s', uuid='%s')%nEnded with an error:%n%s", id, uuid, error);
            default -> String.format("Command(id='%s', uuid='%s')%nWith content:%n'%s'", id, uuid, content);
        };
    }

    public boolean hasAttachments() {
        return attachments != null && !attachments.isEmpty();
    }
}
