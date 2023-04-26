package ru.yandex.practicum.de.kk91.easymoney.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.yandex.practicum.de.kk91.easymoney.config.command.CommandGateway;
import ru.yandex.practicum.de.kk91.easymoney.config.loader.AwsProperties;
import ru.yandex.practicum.de.kk91.easymoney.config.telegram.TelegramBotGateway;
import ru.yandex.practicum.de.kk91.easymoney.data.command.Command;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandAttachment;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandSource;
import ru.yandex.practicum.de.kk91.easymoney.data.command.CommandState;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoaderService {
    private final S3AsyncClient s3AsyncClient;
    private final AwsProperties awsProperties;
    private final TelegramBotGateway telegramBotGateway;
    private final CommandGateway commandGateway;

    public Mono<Void> downloadCommandAttachment(Command command) {
        CommandAttachment attachment = command.getAttachment();
        if (attachment == null) {
            command.setState(CommandState.ERROR);
            command.setContent(
                    "Something went wrong, LoaderService received command without attachments. Original content: " +
                            command.getContent()
            );
            return commandGateway.handleCommand(command);
        }
        if (CommandSource.TELEGRAM_MESSAGE.equals(command.getSource())) {
            String originalUrl = attachment.getUrl();
            return telegramBotGateway.getFileUrl(attachment.getUrl())
                    .map(url -> {
                        attachment.setUrl(url);
                        attachment.setStorageBucket(awsProperties.getS3BucketName());
                        attachment.setFilename(UUID.randomUUID().toString());
                        return attachment;
                    })
                    .flatMap(this::put)
                    .flatMap(value -> {
                        attachment.setStorageETag(value);
                        attachment.setUrl(originalUrl);
                        command.setState(CommandState.LOADED);
                        return commandGateway.handleCommand(command);
                    });
        } else {
            command.setState(CommandState.ERROR);
            command.setContent(
                    "Something went wrong, LoaderService received command from unknown source. Original content: " +
                            command.getContent()
            );
            return commandGateway.handleCommand(command);
        }

    }

    public Mono<String> put(CommandAttachment attachment) {
        log.debug("Start loading attachment '{}'", attachment.toString());
        return Mono.fromFuture(
                s3AsyncClient.putObject(
                        PutObjectRequest.builder()
                                .bucket(awsProperties.getS3BucketName())
                                .contentLength(attachment.getFilesize())
                                .key(attachment.getFilename())
                                .contentType(MediaType.APPLICATION_OCTET_STREAM.toString())
                                .build(),
                        AsyncRequestBody.fromPublisher(download(attachment.getUrl()))
                )
        ).map(PutObjectResponse::eTag);
    }

    private Flux<ByteBuffer> download(String url) {
        log.debug("Start loading file from URL {}", url);
        return WebClient.create()
                .get()
                .uri(url)
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToFlux(ByteBuffer.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(5L)));
    }
}
