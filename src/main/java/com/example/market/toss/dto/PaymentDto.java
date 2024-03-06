package com.example.market.toss.dto;

import lombok.*;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String version;
    private String paymentKey;
    private String type;
    private String orderId;
    private String orderName;
    private String mId;
    private String method;
    private Integer totalAmount;
    private Integer balanceAmount;
    private String status;
}
