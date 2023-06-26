package ru.yandex.practicum.de.kk91.easymoney.data.command.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkCommandDto {
    private UUID uuid;
    private List<SparkCommandAttachmentDto> attachments;
}
