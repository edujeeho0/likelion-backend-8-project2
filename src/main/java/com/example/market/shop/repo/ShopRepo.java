package com.example.market.shop.repo;

import com.example.market.shop.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ShopRepo extends JpaRepository<Shop, Long> {
    @Query("SELECT DISTINCT s " +
            "FROM Shop s JOIN s.items i JOIN i.orders o " +
            "WHERE s.status = :status " +
            "ORDER BY o.createdAt DESC ")
    Page<Shop> findAllByStatus(Shop.Status status, Pageable pageable);

    Optional<Shop> findByOwnerId(Long ownerId);

    @Query("SELECT DISTINCT s " +
            "FROM ShopOpenRequest r JOIN r.shop s " +
            "WHERE r.isApproved IS NULL")
    Page<Shop> findOpenRequested(Pageable pageable);
    @Query("SELECT s " +
            "FROM Shop s " +
            "WHERE s.closeReason IS NOT NULL " +
            "AND s.status != :status")
    Page<Shop> findCloseRequested(Shop.Status status, Pageable pageable);
}
