package ru.yandex.practicum.de.kk91.easymoney.data.transaction;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Invoice;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.InvoiceItem;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Item;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final ItemRepository itemRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    @Transactional
    public Invoice saveParsedInvoice(Invoice invoice) {
        Optional<Invoice> optionalInvoice = invoiceRepository.findInvoiceByExternalId(invoice.getExternalId());
        if (optionalInvoice.isPresent()) {
            return optionalInvoice.get();
        }

        List<InvoiceItem> invoiceItems = invoice.getInvoiceItems();
        List<String> itemExIds = invoiceItems.stream()
                .map(InvoiceItem::getItem)
                .map(Item::getExternalId)
                .toList();

        Map<String, Item> existingItems = itemRepository.findItemsByExternalIdIsIn(itemExIds).stream()
                .collect(Collectors.toMap(Item::getExternalId, Function.identity()));

        for (InvoiceItem invoiceItem : invoiceItems) {
            invoiceItem.setInvoice(invoice);
            Item item = existingItems.get(invoiceItem.getItem().getExternalId());
            if (Objects.nonNull(item)) {
                invoiceItem.setItem(item);
            }
        }

        counterpartyRepository.findCounterpartyByExternalId(invoice.getSeller().getExternalId())
                .ifPresent(invoice::setSeller);

        return invoiceRepository.save(invoice);
    }

}
