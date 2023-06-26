package ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Counterparty;

import java.util.Optional;

public interface CounterpartyRepository extends JpaRepository<Counterparty, Long> {
    Optional<Counterparty> findCounterpartyByExternalId(String externalId);
}
