package com.example.market.shop.repo;

import com.example.market.shop.entity.ShopItemOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopItemOrderRepo extends JpaRepository<ShopItemOrder, Long> {
}
