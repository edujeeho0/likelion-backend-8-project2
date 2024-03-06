package com.example.market.trade.dto;

import com.example.market.trade.entity.TradeItem;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TradeItemDto {
    private Long id;
    private String title;
    private String content;
    private String img;
    private Integer minPrice;

    public static TradeItemDto fromEntity(TradeItem entity) {
        return TradeItemDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .img(entity.getImg())
                .minPrice(entity.getMinPrice())
                .build();
    }
}
