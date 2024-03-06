package com.example.market.toss.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelDto {
    private String cancelReason;
}
