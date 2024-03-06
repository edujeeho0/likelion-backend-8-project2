package com.example.market.trade;

import com.example.market.trade.dto.TradeOfferDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("trades/{itemId}/offers")
@RequiredArgsConstructor
public class OfferController {
    private final OfferService service;

    @PostMapping
    public TradeOfferDto create(
            @PathVariable("itemId")
            Long itemId,
            @RequestBody
            TradeOfferDto dto
    ) {
        return service.create(itemId, dto);
    }

    @GetMapping
    public Page<TradeOfferDto> readOffers(
            @PathVariable("itemId")
            Long itemId,
            Pageable pageable
    ) {
        return service.readOffers(itemId, pageable);
    }

    @PutMapping("{offerId}")
    public TradeOfferDto update(
            @PathVariable("itemId")
            Long itemId,
            @PathVariable("offerId")
            Long offerId,
            @RequestBody
            TradeOfferDto dto
    ) {
        return service.updateOffer(itemId, offerId, dto);
    }
}
