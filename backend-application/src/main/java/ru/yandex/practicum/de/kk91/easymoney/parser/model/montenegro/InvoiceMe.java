package ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceMe {
    private Long id;
    private String iic;
    private BigDecimal totalPrice;
    private BigDecimal totalPriceWithoutVAT;
    private BigDecimal totalVATAmount;
    private Instant dateTimeCreated;
    private List<PaymentMethodMe> paymentMethod;
    private SellerMe seller;
    private List<ItemMe> items;
    @JsonIgnore
    private String content;
}
