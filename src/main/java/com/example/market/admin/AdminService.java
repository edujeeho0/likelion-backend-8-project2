package com.example.market.admin;

import com.example.market.auth.dto.UserDto;
import com.example.market.admin.dto.UserUpgradeDto;
import com.example.market.auth.entity.UserUpgrade;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import com.example.market.shop.dto.OpenRequestDto;
import com.example.market.shop.dto.ShopDto;
import com.example.market.shop.entity.Shop;
import com.example.market.shop.entity.ShopOpenRequest;
import com.example.market.shop.repo.ShopOpenReqRepo;
import com.example.market.shop.repo.ShopRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final ShopRepo shopRepo;
    private final ShopOpenReqRepo openRepo;


    public Page<UserUpgradeDto> listRequests(Pageable pageable) {
        return userUpgradeRepo.findAll(pageable)
                .map(UserUpgradeDto::fromEntity);
    }

    @Transactional
    public UserUpgradeDto approveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(true);
        upgrade.getTarget().setRoles("ROLE_ACTIVE,ROLE_OWNER");
        shopRepo.save(Shop.builder()
                .owner(upgrade.getTarget())
                .build());
        return UserUpgradeDto.fromEntity(upgrade);
    }

    @Transactional
    public UserUpgradeDto disapproveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(false);
        return UserUpgradeDto.fromEntity(upgrade);
    }

    public Page<UserDto> readUsersPage(Pageable pageable) {
        return userRepo.findAll(pageable)
                .map(UserDto::fromEntity);
    }

    public Page<ShopDto> readOpenRequests(Pageable pageable) {
        return shopRepo.findOpenRequested(pageable)
                .map(ShopDto::fromEntity);
    }

    public Page<ShopDto> readCloseRequests(Pageable pageable) {
        return shopRepo.findCloseRequested(Shop.Status.CLOSED, pageable)
                .map(shop -> ShopDto.fromEntity(shop, true));
    }

    @Transactional
    public OpenRequestDto updateShopStatus(
            Long shopId,
            Long reqId,
            OpenRequestDto dto
    ) {
        ShopOpenRequest request = openRepo.findById(reqId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Shop shop = request.getShop();
        if (!shop.getId().equals(shopId))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (!dto.getIsApproved()) {
            if (dto.getReason() == null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            request.setReason(dto.getReason());
        }
        else shop.setStatus(Shop.Status.OPEN);
        request.setIsApproved(dto.getIsApproved());
        return OpenRequestDto.fromEntity(openRepo.save(request));
    }

    public ShopDto approveClose(Long shopId) {
        Shop shop = shopRepo.findById(shopId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (shop.getCloseReason() == null || shop.getStatus() != Shop.Status.OPEN)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);

        shop.setStatus(Shop.Status.CLOSED);
        return ShopDto.fromEntity(shopRepo.save(shop), true);
    }
}
