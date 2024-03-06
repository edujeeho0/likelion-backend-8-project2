package com.example.market.admin;

import com.example.market.auth.dto.UserDto;
import com.example.market.admin.dto.UserUpgradeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<UserDto> readAllUsers(
            Pageable pageable
    ) {
        return service.readUsersPage(pageable);
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
