package com.example.market.auth.exception;

import com.example.market.auth.entity.UserEntity;
import lombok.Getter;

@Getter
public class UserNeedsCaptchaException extends RuntimeException {
    private final UserEntity user;
    public UserNeedsCaptchaException(UserEntity user) {
        super("user is owner");
        this.user = user;
    }
}
