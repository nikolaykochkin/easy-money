package ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemMe {
    private Long id;
    private String name;
    private String code;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal unitPriceAfterVat;
    private BigDecimal priceBeforeVat;
    private Integer vatRate;
    private BigDecimal vatAmount;
    private BigDecimal priceAfterVat;
}
