package ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Invoice;

import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findInvoiceByExternalId(String externalId);
}
