package com.example.market.trade.dto;

import com.example.market.trade.entity.TradeLocation;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TradeLocationDto {
    private Long id;
    private String address;
    private String coordinates;

    public static TradeLocationDto fromEntity(TradeLocation entity) {
        return TradeLocationDto.builder()
                .id(entity.getId())
                .address(entity.getAddress())
                .coordinates(entity.getCoordinates())
                .build();
    }
}
