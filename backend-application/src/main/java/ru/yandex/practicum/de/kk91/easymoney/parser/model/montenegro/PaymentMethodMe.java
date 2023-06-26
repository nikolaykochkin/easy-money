package ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodMe {
    private Long id;
    private String type;
    private BigDecimal amount;
    private String typeCode;
}
