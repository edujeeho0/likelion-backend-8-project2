package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItem extends BaseEntity {
    private String name;
    private String img;
    private String description;
    private Integer price;
    private Integer stock;
}
