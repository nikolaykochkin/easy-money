package ru.yandex.practicum.de.kk91.easymoney.data.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository.AccountRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository.CategoryRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository.CounterpartyRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository.TransactionRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final AccountRepository accountRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;


}
