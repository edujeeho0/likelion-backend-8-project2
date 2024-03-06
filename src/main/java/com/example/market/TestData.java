package com.example.market;

import com.example.market.auth.entity.UserEntity;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopItem;
import com.example.market.shop.repo.ShopItemOrderRepo;
import com.example.market.shop.repo.ShopItemRepo;
import com.example.market.shop.repo.ShopOpenReqRepo;
import com.example.market.shop.repo.ShopRepo;
import com.example.market.trade.repo.TradeItemRepo;
import com.example.market.trade.repo.TradeOfferRepo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TestData {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;

    private final TradeItemRepo tradeItemRepo;
    private final TradeOfferRepo offerRepo;

    private final ShopRepo shopRepo;
    private final ShopItemRepo shopItemRepo;
    private final ShopOpenReqRepo openReqRepo;
    private final ShopItemOrderRepo orderRepo;
    private final PasswordEncoder passwordEncoder;

    public TestData(
            UserRepo userRepo,
            UserUpgradeRepo userUpgradeRepo,
            TradeItemRepo tradeItemRepo,
            TradeOfferRepo offerRepo,
            ShopRepo shopRepo,
            ShopItemRepo shopItemRepo,
            ShopOpenReqRepo openReqRepo,
            ShopItemOrderRepo orderRepo,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepo = userRepo;
        this.userUpgradeRepo = userUpgradeRepo;
        this.tradeItemRepo = tradeItemRepo;
        this.offerRepo = offerRepo;
        this.shopRepo = shopRepo;
        this.shopItemRepo = shopItemRepo;
        this.openReqRepo = openReqRepo;
        this.orderRepo = orderRepo;
        this.passwordEncoder = passwordEncoder;
        testUsers();
        testShops();
        testItems();
    }

    private void testUsers() {
        userRepo.saveAll(List.of(
                UserEntity.builder()
                        .username("inactive")
                        .password(passwordEncoder.encode("test"))
                        .build(),
                UserEntity.builder()
                        .username("normal1")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("edujeeho@gmail.com")
                        .phone("01012345678")
                        .roles("ROLE_ACTIVE")
                        .build(),
                UserEntity.builder()
                        .username("normal2")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("edujeeho@gmail.com")
                        .phone("01012345679")
                        .roles("ROLE_ACTIVE")
                        .build(),
                UserEntity.builder()
                        .username("normal3")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("edujeeho@gmail.com")
                        .phone("01012345679")
                        .roles("ROLE_ACTIVE")
                        .build(),
                UserEntity.builder()
                        .username("owner1")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("edujeeho@gmail.com")
                        .phone("01087654321")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("owner2")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("edujeeho@gmail.com")
                        .phone("01087654322")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("owner3")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("edujeeho@gmail.com")
                        .phone("01087654323")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("test"))
                        .roles("ROLE_ACTIVE,ROLE_ADMIN")
                        .build()
        ));
    }

    private void testShops() {
        userRepo.findAll().stream()
                .filter(user -> user.getRoles().contains("ROLE_OWNER"))
                .forEach(user -> shopRepo.save(Shop.builder()
                        .owner(user)
                        .name(user.getUsername() + "'s shop")
                        .description("description")
                        .category(Shop.Category.DIGITAL)
//                        .status(Shop.Status.PREPARING)
                        .status(Shop.Status.OPEN)
                        .address("59-8 정자1동 Bundang-gu, Seongnam-si, Gyeonggi-do")
                        .coordinates("127.1146267,37.3699127")
                        .build()));

    }

    private void testItems() {
        shopRepo.findAll()
                .forEach(this::addItems);
    }

    private void addItems(Shop shop) {
        List<String> items = List.of(
                "keyboard",
                "speaker",
                "mouse",
                "monitor"
        );
        items.forEach(item -> shopItemRepo.save(ShopItem.builder()
                .shop(shop)
                .name(item + shop.getId())
                .description(shop.getName() + " " + item)
                .price((int) (shop.getId() * 10000 + item.length() * 10000))
                .stock(10)
                .build()));
    }
}
