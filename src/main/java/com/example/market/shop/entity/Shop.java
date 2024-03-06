package com.example.market.shop.entity;

import com.example.market.auth.entity.UserEntity;
import com.example.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Shop extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity owner;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Category category;
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Category {
        FOOD, FASHION, DIGITAL, SPORTS, FURNISHING
    }

    public enum Status {
        PREPARING, OPEN, CLOSED
    }
}
