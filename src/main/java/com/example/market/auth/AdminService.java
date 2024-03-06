package com.example.market.auth;

import com.example.market.auth.dto.UserDto;
import com.example.market.auth.dto.UserUpgradeDto;
import com.example.market.auth.entity.UserUpgrade;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;

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
        return UserUpgradeDto.fromEntity(upgrade);
    }

    @Transactional
    public UserUpgradeDto disapproveUpgrade(Long id) {
        UserUpgrade upgrade = userUpgradeRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        upgrade.setApproved(false);
        return UserUpgradeDto.fromEntity(upgrade);
    }

    public List<UserDto> readAllUsers() {
        return userRepo.findAll().stream()
                .map(UserDto::fromEntity)
                .toList();
    }
}
