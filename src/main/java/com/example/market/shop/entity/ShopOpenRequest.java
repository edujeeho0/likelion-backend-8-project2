package com.example.market.shop.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ShopOpenRequest extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private Shop shop;
    private Boolean isApproved;
    private String reason;
}
