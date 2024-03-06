package com.example.market.shop.dto;

import com.example.market.shop.entity.ShopItem;
import lombok.*;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopItemDto {
    private Long id;
    private Long shopId;
    private String img;
    private String description;
    private Integer price;
    private Integer stock;

    public static ShopItemDto fromEntity(ShopItem entity) {
        return ShopItemDto.builder()
                .id(entity.getId())
                .img(entity.getImg())
                .description(entity.getDescription())
                .price(entity.getPrice())
                .stock(entity.getStock())
                .build();
    }
}
