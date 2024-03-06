package com.example.market.shop;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.entity.ShopItemOrder;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final ShopItemOrderRepo orderRepo;

    public ItemOrderDto createOrder(ItemOrderDto dto) {
        ShopItem item = itemRepo.findById(dto.getItemId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        return ItemOrderDto.fromEntity(orderRepo.save(ShopItemOrder.builder()
                .item(item)
                .orderUser(user)
                .address(dto.getAddress())
                .count(dto.getCount())
                .totalPrice(item.getPrice() * dto.getCount())
                .build()));
    }

    public ItemOrderDto readOne(Long orderId) {
        return ItemOrderDto.fromEntity(getOrder(orderId));
    }

    public Page<ItemOrderDto> myOrders(Pageable pageable) {
        Long userId = authFacade.extractUser().getId();
        return orderRepo.findAllByOrderUserId(userId, pageable)
                .map(ItemOrderDto::fromEntity);
    }

    public Page<ItemOrderDto> myShopOrders(Pageable pageable) {
        UserEntity user = authFacade.extractUser();
        if (!Arrays.asList(user.getRoles().split(",")).contains("ROLE_OWNER"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Long shopId = shopRepo.findByOwnerId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getId();
        return orderRepo.findAllByShopId(shopId, pageable)
                .map(ItemOrderDto::fromEntity);
    }

    @Transactional
    public ItemOrderDto updateState(Long orderId, ItemOrderDto dto) {
        ShopItemOrder order = getOrder(orderId);
        if (!order.getStatus().equals(ShopItemOrder.Status.ORDERED))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        UserEntity user = authFacade.extractUser();
        switch (dto.getStatus()) {
            case ORDERED ->
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            case DECLINED -> {
                if (!order.getItem()
                        .getShop()
                        .getOwner()
                        .getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setReason(dto.getReason());
                order.setStatus(dto.getStatus());
            }
            case ACCEPTED -> {
                if (!order.getItem()
                        .getShop()
                        .getOwner()
                        .getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setStatus(dto.getStatus());
                order.getItem().decreaseStock(dto.getCount());
            }
            case CANCELED -> {
                if (!order.getOrderUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                if (order.getStatus().equals(ShopItemOrder.Status.ACCEPTED))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                order.setStatus(dto.getStatus());
            }
        }
        return ItemOrderDto.fromEntity(orderRepo.save(order));
    }


    private ShopItemOrder getOrder(Long orderId) {
        ShopItemOrder order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!order.getOrderUser()
                .getId().equals(user.getId()) &&
            !order.getItem()
                    .getShop()
                    .getOwner()
                    .getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        return order;
    }
}
