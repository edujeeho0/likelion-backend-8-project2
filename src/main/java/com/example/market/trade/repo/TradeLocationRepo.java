package com.example.market.trade.repo;

import com.example.market.trade.entity.TradeLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeLocationRepo extends JpaRepository<TradeLocation, Long> {
    Boolean existsByOfferIdAndStatus(Long offerId, TradeLocation.Status status);
}
