package com.example.market.shop.repo;

import com.example.market.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepo extends JpaRepository<Shop, Long> {
}
