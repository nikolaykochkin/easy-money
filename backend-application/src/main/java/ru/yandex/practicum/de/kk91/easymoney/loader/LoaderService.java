package ru.yandex.practicum.de.kk91.easymoney.loader;

import io.awspring.cloud.s3.S3Resource;
import io.awspring.cloud.s3.S3Template;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandAttachment;
import ru.yandex.practicum.de.kk91.easymoney.data.command.entity.CommandState;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoaderService {
    @Value("${easy-money.loader.bucket}")
    private String bucket;
    private final S3Template s3Template;

    public void uploadCommandAttachments(Command command) throws IOException {
        Assert.isTrue(command.hasAttachments(), "command attachments not found");

        for (CommandAttachment attachment : command.getAttachments()) {
            // Attachment has already uploaded
            if (Objects.nonNull(attachment.getStorageId())) {
                continue;
            }

            Assert.notNull(attachment.getUrl(), "attachment url is required");

            String key = getAttachmentKey(attachment);
            URI uri = URI.create(attachment.getUrl());

            S3Resource uploaded = s3Template.upload(bucket, key, uri.toURL().openStream());

            attachment.setStorageId(key);
            attachment.setMimeType(uploaded.contentType());
            attachment.setFilesize(uploaded.contentLength());
            attachment.setUrl(uploaded.getURL().toString());

            deleteLocalFile(uri);
        }

        command.setState(CommandState.LOADED);
    }

    // TODO: 15.6.23. Delete directory
    private void deleteLocalFile(URI uri) {
        if ("file".equalsIgnoreCase(uri.getScheme())) {
            try {
                Files.deleteIfExists(Path.of(uri));
            } catch (IOException e) {
                log.error("Couldn't delete local file by uri: {}. Cause: {}", uri, e.getMessage(), e);
            }
        }
    }

    private String getAttachmentKey(CommandAttachment attachment) {
        String id = Optional.ofNullable(attachment.getId()).map(Object::toString).orElse("_");
        String filename = Optional.ofNullable(attachment.getFilename()).orElse(UUID.randomUUID().toString());
        return String.format("%s/%s/%s", attachment.getUuid(), id, filename);
    }
}
