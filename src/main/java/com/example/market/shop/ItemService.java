package com.example.market.shop;

import com.example.market.FileHandlerUtils;
import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ShopItemDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final FileHandlerUtils fileHandlerUtils;

    public ShopItemDto create(
            Long shopId,
            ShopItemDto dto
    ) {
        Shop shop = checkOwner(shopId);
        return ShopItemDto.fromEntity(itemRepo.save(ShopItem.builder()
                .shop(shop)
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stock(dto.getStock())
                .build()));
    }

    public Page<ShopItemDto> readPage(
            Long shopId,
            Pageable pageable
    ) {
        checkShopStatus(shopId);
        return itemRepo.findAllByShopId(shopId, pageable)
                .map(ShopItemDto::fromEntity);
    }

    public ShopItemDto readOne(
            Long shopId,
            Long itemId
    ) {
        checkShopStatus(shopId);
        return ShopItemDto.fromEntity(getItem(shopId, itemId));
    }

    @Transactional
    public ShopItemDto update(
            Long shopId,
            Long itemId,
            ShopItemDto itemDto
    ) {
        ShopItem item = getItemOwner(shopId, itemId);
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setPrice(itemDto.getPrice());
        item.setStock(itemDto.getStock());
        return ShopItemDto.fromEntity(itemRepo.save(item));
    }

    public ShopItemDto updateImg(
            Long shopId,
            Long itemId,
            MultipartFile file
    ) {
        ShopItem item = getItemOwner(shopId, itemId);
        String requestPath = fileHandlerUtils.saveFile(
                String.format("shops/%d/items/%d/", shopId, itemId),
                "image",
                file
        );
        item.setImg(requestPath);
        return ShopItemDto.fromEntity(itemRepo.save(item));
    }

    @Transactional
    public void delete(
            Long shopId,
            Long itemId
    ) {
        ShopItem item = getItemOwner(shopId, itemId);
        itemRepo.delete(item);
    }

    private void checkShopStatus(Long shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (
                !shop.getStatus().equals(Shop.Status.OPEN) &&
                !shop.getOwner().getId().equals(user.getId())
        )
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    private ShopItem getItem(Long shopId, Long itemId) {
        ShopItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!item.getShop().getId().equals(shopId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return item;
    }

    private ShopItem getItemOwner(Long shopId, Long itemId) {
        ShopItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Shop shop = item.getShop();
        if (!shop.getId().equals(shopId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        UserEntity user = authFacade.extractUser();
        if (!shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return item;
    }

    private Shop checkOwner(Long id) {
        Shop shop = shopRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return shop;
    }
}
