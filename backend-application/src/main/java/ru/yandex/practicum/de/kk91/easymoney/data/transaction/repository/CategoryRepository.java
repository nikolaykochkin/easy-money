package ru.yandex.practicum.de.kk91.easymoney.data.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.de.kk91.easymoney.data.transaction.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
