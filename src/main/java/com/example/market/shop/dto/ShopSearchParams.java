package com.example.market.shop.dto;

import com.example.market.shop.entity.Shop;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ShopSearchParams {
    private String name;
    private Shop.Category category;
}
