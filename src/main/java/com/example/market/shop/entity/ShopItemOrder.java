package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopItemOrder extends BaseEntity {
    private Integer count;
    private Integer totalPrice;
    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.ORDERED;
    @Setter
    private String reason;

    public enum Status {
        ORDERED, ACCEPTED, DECLINED,
    }
}
