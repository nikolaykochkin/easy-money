package ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findItemByExternalId(String externalId);

    List<Item> findItemsByExternalIdIsIn(List<String> externalIds);
}
