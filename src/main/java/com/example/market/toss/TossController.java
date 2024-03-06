package com.example.market.toss;

import com.example.market.shop.ItemService;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.toss.dto.PaymentConfirmDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TossController {
//    private final ItemService itemService;
    private final ShopItemRepo itemRepo;

    @PostMapping("/toss/confirm-payment")
    @ResponseBody
    public Object confirmPayment(
            @RequestBody
            PaymentConfirmDto dto,
            @RequestHeader("Authorization")
            String authorization
    ) {
        log.info(authorization);
        log.info("received: {}", dto.toString());
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED);
//        return service.confirmPayment(dto);
    }

    // 토스 결제 전용 Endpoint
    @GetMapping("/items/{id}")
    @ResponseBody
    public ShopItemDto readOne(
            @PathVariable("id")
            Long id
    ) {
        return itemRepo.findById(id)
                .map(ShopItemDto::fromEntity)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}