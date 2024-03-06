package com.example.market.auth;

import com.example.market.auth.dto.CreateUserDto;
import com.example.market.auth.dto.JwtRequestDto;
import com.example.market.auth.dto.JwtResponseDto;
import com.example.market.auth.dto.UpdateUserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    @PutMapping("signup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signUpFinal(
            @RequestBody
            UpdateUserDto dto
    ) {
        userService.updateUser(dto);
    }
}
