package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItemOrder extends BaseEntity {
    private Integer count;
    private Integer totalPrice;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String reason;

    public enum Status {
        ORDERED, ACCEPTED, DECLINED,
    }
}
