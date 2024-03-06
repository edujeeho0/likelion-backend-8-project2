package com.example.market.shop;

import com.example.market.shop.dto.ItemSearchParams;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.dto.ShopSearchParams;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.repo.QItemRepo;
import com.example.market.shop.repo.QShopRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {
    private final QItemRepo qItemRepo;
    private final QShopRepo qShopRepo;
    private final ShopService shopService;


    public Page<ShopDto> searchShops(ShopSearchParams params, Pageable pageable) {
        if (params.getName() == null && params.getCategory() == null)
            return shopService.readPage(pageable);
        return qShopRepo.searchShops(params, pageable)
                .map(ShopDto::fromEntity);
    }

    public Page<ShopItemDto> searchItems(ItemSearchParams params, Pageable pageable) {
        return qItemRepo.searchItems(params, pageable)
                .map(ShopItemDto::fromEntity);
    }
}
