package com.example.market.trade.repo;

import com.example.market.trade.entity.TradeOffer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TradeOfferRepo extends JpaRepository<TradeOffer, Long> {
    Page<TradeOffer> findAllByUserId(Long userId, Pageable pageable);
    List<TradeOffer> findAllByIdNot(Long id);
}
