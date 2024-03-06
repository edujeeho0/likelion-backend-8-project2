package com.example.market.auth.controller;

import com.example.market.auth.AdminService;
import com.example.market.auth.dto.UserDto;
import com.example.market.auth.dto.UserUpgradeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService service;

    @GetMapping("users")
    public List<UserDto> readAllUsers() {
        return service.readAllUsers();
    }

    @GetMapping("upgrades")
    public List<UserUpgradeDto> upgradeRequests() {
        return service.listRequests();
    }

    @PutMapping("upgrades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserUpgradeDto approve(
            @PathVariable("id")
            Long id
    ) {
        return service.approveUpgrade(id);
    }

    @DeleteMapping("upgrades/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public UserUpgradeDto disapprove(
            @PathVariable("id")
            Long id
    ) {
        return service.disapproveUpgrade(id);
    }
}
