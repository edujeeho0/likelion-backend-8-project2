package com.example.market.trade.dto;

import com.example.market.trade.entity.TradeOffer;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TradeOfferDto {
    private Long id;
    private Integer offerPrice;
    private TradeOffer.Status status;

    public static TradeOfferDto fromEntity(TradeOffer entity) {
        return TradeOfferDto.builder()
                .id(entity.getId())
                .offerPrice(entity.getOfferPrice())
                .status(entity.getStatus())
                .build();
    }
}
