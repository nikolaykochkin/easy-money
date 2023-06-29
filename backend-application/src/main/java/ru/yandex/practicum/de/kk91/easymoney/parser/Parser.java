package ru.yandex.practicum.de.kk91.easymoney.parser;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Invoice;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.InvoiceItem;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Item;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Counterparty;
import ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro.InvoiceMe;
import ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro.ItemMe;
import ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro.PaymentMethodMe;
import ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro.SellerMe;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class Parser {
    private static final String IIC = "iic";
    private static final String CRTD = "crtd";
    private static final String DATE_TIME_CREATED = "dateTimeCreated";
    private static final String TIN = "tin";
    private static final Set<String> KEYS = Set.of(IIC, CRTD, TIN);
    private static final Currency EURO = Currency.getInstance("EUR");

    private final ObjectMapper objectMapper;
    private final String montenegroBaseUrl;
    private final WebClient webClientMontenegro;

    public Parser(@Value("${easy-money.parser.montenegro.baseUrl}") String montenegroBaseUrl,
                  WebClient webClientMontenegro) {
        this.montenegroBaseUrl = montenegroBaseUrl;
        this.webClientMontenegro = webClientMontenegro;
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
    }

    public Optional<Invoice> parseInvoice(String url) {
        if (StringUtils.startsWithIgnoreCase(url, montenegroBaseUrl)) {
            return parseMontenegroInvoice(url);
        }
        return Optional.empty();
    }

    private Optional<Invoice> parseMontenegroInvoice(String url) {
        return getRequestBodyMe(url)
                .flatMap(body -> retrieveInvoiceMontenegro(url, body))
                .flatMap(this::parseInvoiceMe)
                .map(invoiceMe -> invoiceMeToInvoice(invoiceMe, url));
    }

    private Invoice invoiceMeToInvoice(InvoiceMe invoiceMe, String url) {
        List<InvoiceItem> invoiceItems = invoiceMe.getItems().stream()
                .map(this::itemMeToInvoiceItem)
                .toList();

        String paymentMethod = Optional.ofNullable(invoiceMe.getPaymentMethod())
                .map(l -> l.get(0))
                .map(PaymentMethodMe::getTypeCode)
                .orElse(null);

        return Invoice.builder()
                .dateTime(invoiceMe.getDateTimeCreated())
                .content(invoiceMe.getContent())
                .externalId(invoiceMe.getIic())
                .invoiceItems(invoiceItems)
                .seller(sellerToCounterparty(invoiceMe.getSeller()))
                .paymentMethod(paymentMethod)
                .url(url)
                .currency(EURO)
                .totalPrice(invoiceMe.getTotalPrice())
                .build();
    }

    private Counterparty sellerToCounterparty(SellerMe sellerMe) {
        return Counterparty.builder()
                .name(sellerMe.getName())
                .externalId(sellerMe.getIdNum())
                .build();
    }

    private InvoiceItem itemMeToInvoiceItem(ItemMe itemMe) {
        return InvoiceItem.builder()
                .item(Item.builder()
                        .name(itemMe.getName())
                        .externalId(itemMe.getCode())
                        .build())
                .price(itemMe.getPriceAfterVat())
                .unit(itemMe.getUnit())
                .quantity(itemMe.getQuantity())
                .unitPrice(itemMe.getUnitPriceAfterVat())
                .build();
    }

    private Optional<InvoiceMe> parseInvoiceMe(String json) {
        try {
            InvoiceMe invoiceMe = objectMapper.readValue(json, InvoiceMe.class);
            invoiceMe.setContent(json);
            return Optional.of(invoiceMe);
        } catch (Exception e) {
            log.error("Failed to parse InvoiceMe from JSON: {}. Cause: {}", json, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<String> retrieveInvoiceMontenegro(String url, String body) {
        try {
            return webClientMontenegro.post()
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .blockOptional();
        } catch (Exception e) {
            log.error("Failed to retrieve invoice from Montenegro by url: {} with body: {}. Cause: {}.", url, body, e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Optional<String> getRequestBodyMe(String url) {
        String[] parts = url.split("\\?");
        if (parts.length != 2) {
            log.error("URL is not valid: {}", url);
            return Optional.empty();
        }
        Map<String, String> params = Arrays.stream(parts[1].split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(s -> s[0], s -> s[1]));

        if (!params.keySet().containsAll(KEYS)) {
            log.error("URL doesn't contain all necessary params. {}", url);
            return Optional.empty();
        }

        String body = String.format("%s=%s&%s=%s&%s=%s",
                IIC, params.get(IIC),
                DATE_TIME_CREATED, params.get(CRTD),
                TIN, params.get(TIN)
        );
        return Optional.of(body);
    }
}
