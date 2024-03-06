package com.example.market.trade.entity;

import com.example.market.auth.entity.UserEntity;
import com.example.market.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class TradeItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    @Setter
    private String title;
    @Setter
    private String content;
    @Setter
    private String img;
    @Setter
    private Integer minPrice;
    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.ON_SALE;
    public enum Status {
        ON_SALE, SOLD
    }
}
