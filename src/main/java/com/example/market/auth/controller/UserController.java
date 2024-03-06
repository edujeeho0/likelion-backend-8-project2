package com.example.market.auth.controller;

import com.example.market.auth.JpaUserService;
import com.example.market.auth.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {
    private final JpaUserService userService;

    @PostMapping("signin")
    public JwtResponseDto signIn(
            @RequestBody
            JwtRequestDto dto
    ) {
        return userService.signin(dto);
    }

    @PostMapping("signup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signUp(
            @RequestBody
            CreateUserDto dto
    ) {
        userService.createUser(dto);
    }

    @PutMapping("details")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signUpFinal(
            @RequestBody
            UpdateUserDto dto
    ) {
        userService.updateUser(dto);
    }

    @PutMapping("upgrade")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upgrade(
            @RequestBody
            RequestUpgradeDto dto
    ) {
        userService.upgradeRoleRequest(dto);
    }
}
