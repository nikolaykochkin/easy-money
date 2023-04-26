package ru.yandex.practicum.de.kk91.easymoney.data.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.util.MimeType;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "attachments", schema = "command")
public class CommandAttachment {
    private static final String ID_FIELD = "id";
    private static final String UUID_FIELD = "uuid";
    private static final String URL_FIELD = "url";
    private static final String STORAGE_ID_FIELD = "storage_id";
    private static final String STORAGE_BUCKET_FIELD = "storage_bucket";
    private static final String STORAGE_E_TAG_FIELD = "e_tag";
    private static final String FILENAME_FIELD = "filename";
    private static final String FILESIZE_FIELD = "filesize";
    private static final String MIME_TYPE_FIELD = "mime_type";

    @Id
    @Column(ID_FIELD)
    @JsonProperty(ID_FIELD)
    private Long id;

    @Column(UUID_FIELD)
    @JsonProperty(UUID_FIELD)
    private UUID uuid;

    @Column(URL_FIELD)
    @JsonProperty(URL_FIELD)
    private String url;

    @Column(STORAGE_ID_FIELD)
    @JsonProperty(STORAGE_ID_FIELD)
    private String storageId;

    @Column(STORAGE_BUCKET_FIELD)
    @JsonProperty(STORAGE_BUCKET_FIELD)
    private String storageBucket;

    @Column(STORAGE_E_TAG_FIELD)
    @JsonProperty(STORAGE_E_TAG_FIELD)
    private String storageETag;

    @Column(FILENAME_FIELD)
    @JsonProperty(FILENAME_FIELD)
    private String filename;

    @Column(FILESIZE_FIELD)
    @JsonProperty(FILESIZE_FIELD)
    private Long filesize;

    @Column(MIME_TYPE_FIELD)
    @JsonProperty(MIME_TYPE_FIELD)
    private String mimeType;
}
