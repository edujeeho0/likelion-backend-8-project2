package com.example.market.toss.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmDto {
    private String paymentKey;
    private String orderId;
    private Integer amount;
}
