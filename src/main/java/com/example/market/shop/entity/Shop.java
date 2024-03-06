package com.example.market.shop.entity;

import com.example.market.auth.entity.UserEntity;
import com.example.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Shop extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    private UserEntity owner;
    @Setter
    private String name;
    @Setter
    private String description;
    @Setter
    @Enumerated(EnumType.STRING)
    private Category category;
    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.PREPARING;

    @Setter
    private Boolean closed;
    @Setter
    private String closeReason;
    public enum Category {
        FOOD, FASHION, DIGITAL, SPORTS, FURNISHING
    }

    public enum Status {
        PREPARING, OPEN, CLOSED
    }
}
