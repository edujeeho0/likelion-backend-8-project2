package com.example.market.toss;

import com.example.market.auth.AuthenticationFacade;
import com.example.market.auth.entity.UserEntity;
import com.example.market.shop.dto.ItemOrderDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.entity.ShopItemOrder;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.toss.dto.PaymentConfirmDto;
import com.example.market.toss.dto.PaymentDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossService {
    private final AuthenticationFacade authFacade;
    private final TossHttpService httpService;
    private final ShopItemRepo itemRepo;
    private final ShopItemOrderRepo orderRepo;

    @Transactional
    public Object confirmPayment(PaymentConfirmDto dto) {
        PaymentDto tossPayment = httpService.getPayment(dto.getPaymentKey());
        String orderName = tossPayment.getOrderName();
        Long itemId = Long.parseLong(orderName.split("-")[0]);
        ShopItem item = getItem(itemId);
        if (tossPayment.getTotalAmount() % item.getPrice() != 0)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        int count = tossPayment.getTotalAmount() / item.getPrice();
        UserEntity user = authFacade.extractUser();
        item.decreaseStock(count);
        return ItemOrderDto.fromEntity(orderRepo.save(ShopItemOrder.builder()
                .item(item)
                .orderUser(user)
                .count(count)
                .totalPrice(tossPayment.getTotalAmount())
                .build()));
    }

    private ShopItem getItem(Long itemId) {
        ShopItem item = itemRepo.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (item.getShop().getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        return item;
    }
}
