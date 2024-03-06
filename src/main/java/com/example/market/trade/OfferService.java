package com.example.market.trade;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.trade.dto.TradeItemDto;
import com.example.market.trade.dto.TradeOfferDto;
import com.example.market.trade.entity.TradeItem;
import com.example.market.trade.entity.TradeOffer;
import com.example.market.trade.repo.TradeItemRepo;
import com.example.market.trade.repo.TradeOfferRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferService {
    private final TradeItemRepo itemRepo;
    private final TradeOfferRepo offerRepo;
    private final AuthenticationFacade authFacade;

    public TradeOfferDto create(Long itemId, TradeOfferDto dto) {
        TradeItem item = itemRepo.findById(itemId)
            .orElseThrow(() ->new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        return TradeOfferDto.fromEntity(offerRepo.save(TradeOffer.builder()
                .user(user)
                .item(item)
                .offerPrice(dto.getOfferPrice())
                .build()));
    }

    public Page<TradeOfferDto> readOffers(Long itemId, Pageable pageable) {
        TradeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (item.getUser().getId().equals(user.getId()))
            return offerRepo.findAll(pageable)
                    .map(TradeOfferDto::fromEntity);
        else return offerRepo.findAllByUserId(user.getId(), pageable)
                .map(TradeOfferDto::fromEntity);
    }

    @Transactional
    public TradeOfferDto updateOffer(
            Long itemId,
            Long offerId,
            TradeOfferDto dto
    ) {
        TradeItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        TradeOffer offer = offerRepo.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!item.getId().equals(offer.getItem().getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        UserEntity user = authFacade.extractUser();
        TradeOffer.Status status = dto.getStatus();
        switch (status) {
            case OFFERED
                    -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            case ACCEPTED, DECLINED -> {
                // 물품을 등록한 사용자가 아니면 수락 또는 거절 불가
                if (!user.getId().equals(item.getUser().getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                // 제안된 제안이 아니면 수락 또는 거절 불가
                if (offer.getStatus() != TradeOffer.Status.OFFERED)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }
            case CONFIRMED -> {
                if (!offer.getUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                // 확정시 다른 제안은 전부 거절
                offerRepo.findAllByIdNot(offerId)
                        .forEach(other -> other.setStatus(TradeOffer.Status.DECLINED));
                // 물품은 판매 완료
                item.setStatus(TradeItem.Status.SOLD);
            }
        }
        offer.setStatus(status);
        return TradeOfferDto.fromEntity(offer);
    }
}
