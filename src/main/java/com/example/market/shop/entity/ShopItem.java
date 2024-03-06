package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;
    private String name;
    private String img;
    private String description;
    private Integer price;
    private Integer stock;
}
