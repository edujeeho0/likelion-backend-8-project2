package com.example.market.shop.dto;

import lombok.*;

@Getter
@Setter
@ToString
public class ItemSearchParams {
    private String name;
    private Integer priceFloor;
    private Integer priceCeil;
}
