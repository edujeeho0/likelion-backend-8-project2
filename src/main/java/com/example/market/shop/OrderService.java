package com.example.market.shop;

import com.example.market.alert.AlertService;
import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.entity.ShopItemOrder;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopRepo;
import com.example.market.toss.TossHttpService;
import com.example.market.toss.dto.PaymentCancelDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final AuthenticationFacade authFacade;
    private final ShopRepo shopRepo;
    private final ShopItemRepo itemRepo;
    private final ShopItemOrderRepo orderRepo;
    private final TossHttpService tossHttpService;
    private final AlertService alertService;

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

    public Page<ItemOrderDto> myShopOrders(Long shopId, Pageable pageable) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        UserEntity user = authFacade.extractUser();
        if (!shop.getOwner().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
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
            case VISIT -> {
                if (!order.getOrderUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setStatus(dto.getStatus());
            }
            case DECLINED -> {
                if (!order.getItem()
                        .getShop()
                        .getOwner()
                        .getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                if (dto.getReason() == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                order.setReason(dto.getReason());
                order.setStatus(dto.getStatus());
//                tossHttpService.cancelPayment(
//                        order.getPaymentKey(),
//                        PaymentCancelDto.builder()
//                                .cancelReason(String.format("DECLINED: %s", dto.getReason()))
//                                .build()
//                );
            }
            case ACCEPTED -> {
                if (!order.getItem()
                        .getShop()
                        .getOwner()
                        .getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                order.setStatus(dto.getStatus());
                order.getItem().decreaseStock(order.getCount());
//                alertService.sendPurchaseAcceptAlert(orderId);
            }
            case CANCELED -> {
                if (!order.getOrderUser().getId().equals(user.getId()))
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN);
                if (order.getStatus().equals(ShopItemOrder.Status.ACCEPTED))
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
                order.setStatus(dto.getStatus());
//                tossHttpService.cancelPayment(
//                        order.getPaymentKey(),
//                        PaymentCancelDto.builder()
//                                .cancelReason(String.format("CANCELED: %s", dto.getReason()))
//                                .build()
//                );
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
