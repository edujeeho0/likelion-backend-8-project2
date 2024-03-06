package com.example.market.shop.repo;

import com.example.market.shop.entity.ShopOpenRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopOpenReqRepo extends JpaRepository<ShopOpenRequest, Long> {
}
