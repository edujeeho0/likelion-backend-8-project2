package com.example.market.shop.repo;

import com.example.market.shop.dto.ItemSearchParams;
import com.example.market.shop.entity.ShopItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QItemRepo {
    Page<ShopItem> searchItems(ItemSearchParams params, Pageable pageable);
}
