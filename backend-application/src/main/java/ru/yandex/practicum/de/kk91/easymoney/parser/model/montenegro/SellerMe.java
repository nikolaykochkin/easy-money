package ru.yandex.practicum.de.kk91.easymoney.parser.model.montenegro;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerMe {
    private String idType;
    private String idNum;
    private String name;
    private String address;
    private String town;
    private String country;
}
