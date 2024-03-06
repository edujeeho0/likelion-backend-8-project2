package com.example.market.shop.dto;

import com.example.market.shop.entity.Shop;
import lombok.*;
import org.springframework.stereotype.Service;

@Getter
@Setter
@ToString
public class ShopSearchParams {
    private String name;
    private Shop.Category category;
}
