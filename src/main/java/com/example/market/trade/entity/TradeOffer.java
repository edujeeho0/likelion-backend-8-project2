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
public class TradeOffer extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    @ManyToOne(fetch = FetchType.LAZY)
    private TradeItem item;

    private Integer offerPrice;
    @Setter
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Status status = Status.OFFERED;

    public enum Status {
        OFFERED,
        ACCEPTED,
        DECLINED,
        CONFIRMED
    }
}
