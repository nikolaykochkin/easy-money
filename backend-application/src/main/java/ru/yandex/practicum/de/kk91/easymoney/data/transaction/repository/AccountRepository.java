package ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
