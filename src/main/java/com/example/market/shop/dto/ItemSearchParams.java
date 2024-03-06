package com.example.market.shop.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ItemSearchParams {
    private String name;
    private Integer priceFloor;
    private Integer priceCeil;
}
