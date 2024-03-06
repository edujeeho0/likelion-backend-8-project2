package com.example.market.toss;

import com.example.market.toss.dto.PaymentCancelDto;
import com.example.market.toss.dto.PaymentConfirmDto;
import com.example.market.toss.dto.PaymentDto;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/payments")
public interface TossHttpService {
    @PostExchange("/confirm")
    PaymentDto confirmPayment(
            @RequestBody
            PaymentConfirmDto dto
    );

    @GetExchange("/{paymentKey}")
    PaymentDto getPayment(
            @PathVariable("paymentKey")
            String paymentKey
    );

    @PostExchange("/{paymentKey}/cancel")
    PaymentDto cancelPayment(
            @PathVariable("paymentKey")
            String paymentKey,
            @RequestBody
            PaymentCancelDto dto
    );
}