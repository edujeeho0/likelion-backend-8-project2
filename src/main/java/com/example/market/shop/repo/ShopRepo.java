package com.example.market.shop.repo;

import com.example.market.shop.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ShopRepo extends JpaRepository<Shop, Long> {
    Page<Shop> findAllByStatus(Shop.Status status, Pageable pageable);
    @Query("SELECT s " +
            "FROM Shop s " +
            "WHERE s.closeReason IS NOT NULL " +
            "AND s.status != :status")
    Page<Shop> findCloseRequested(Shop.Status status, Pageable pageable);
}
