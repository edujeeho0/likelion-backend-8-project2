package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItem extends BaseEntity {
    private String name;
    private String img;
    private String description;
    private Integer price;
    private Integer count;
}
