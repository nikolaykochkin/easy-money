package ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import ru.yandex.practicum.de.kk91.easymoney.data.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "invoice")
public class Invoice {

    private static final String REPORT_TEMPLATE =
            "Invoice%nID: %d%nDate: %s%nURL: %s%nPaid by: %s%nCurrency: %s%nSeller: %s%nAccount: %s%nTOTAL: %.2f";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private UUID uuid;
    private Instant dateTime;
    private String externalId;
    private String url;
    @JdbcTypeCode(SqlTypes.JSON)
    private String content;
    private String paymentMethod;
    private Currency currency;
    private BigDecimal totalPrice;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "seller_id")
    private Counterparty seller;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id")
    private List<InvoiceItem> invoiceItems;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "invoice_id")
    private List<Transaction> transactions;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreatedDate
    private Instant createdDate;

    @LastModifiedDate
    private Instant lastModifiedDate;

    public String getUserReport() {
        String account = Optional.ofNullable(getAccount())
                .map(Account::getName)
                .orElse("unknown");
        String date = DateTimeFormatter.ISO_INSTANT.format(dateTime);
        return String.format(REPORT_TEMPLATE, id, date, url, paymentMethod, currency, seller.getName(), account, totalPrice);
    }
}
