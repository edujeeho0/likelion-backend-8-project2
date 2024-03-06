package com.example.market.admin;

import com.example.market.auth.dto.UserDto;
import com.example.market.admin.dto.UserUpgradeDto;
import com.example.market.auth.entity.UserUpgrade;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import com.example.market.shop.entity.Shop;
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

    public List<UserUpgradeDto> listRequests() {
        return userUpgradeRepo.findAll().stream()
                .map(UserUpgradeDto::fromEntity)
                .toList();
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
}
