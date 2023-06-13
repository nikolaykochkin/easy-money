package ru.yandex.practicum.de.kk91.easymoney.data.telegram;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.yandex.practicum.de.kk91.easymoney.data.user.User;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "telegram_message")
public class TelegramMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private UUID uuid;
    private Instant messageDate;
    private Integer updateId;
    private Integer messageId;
    private Long chatId;
    private Long tgUserId;
    private String voiceFilePath;
    private String photoFilePath;
    private String text;

    @JdbcTypeCode(SqlTypes.JSON)
    private String update;

    @JdbcTypeCode(SqlTypes.JSON)
    private TelegramFileMetadata fileMetadata;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;
}