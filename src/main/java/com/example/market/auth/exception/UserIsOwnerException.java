package com.example.market.auth.exception;

import com.example.market.auth.entity.UserEntity;
import lombok.Getter;

public class UserIsOwnerException extends RuntimeException {
    @Getter
    private final UserEntity user;
    public UserIsOwnerException(UserEntity user) {
        super("user is owner");
        this.user = user;
    }
}
