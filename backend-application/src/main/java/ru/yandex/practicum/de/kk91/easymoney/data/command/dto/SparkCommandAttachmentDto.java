package ru.yandex.practicum.de.kk91.easymoney.data.command.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparkCommandAttachmentDto {
    private Long id;
    private String decoded;
}
