package com.example.market.shop.repo;

import com.example.market.shop.dto.ShopSearchParams;
import com.example.market.shop.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QShopRepo {
    Page<Shop> searchShops(ShopSearchParams params, Pageable pageable);
}
