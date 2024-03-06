package com.example.market.auth.dto;

import com.example.market.auth.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private Integer age;
    private String email;
    private String phone;
    private String profileImg;
    private List<String> roles;

    public static UserDto fromEntity(UserEntity entity) {
        List<String> roles = Arrays.stream(entity.getRoles().split(","))
                .toList();

        return UserDto.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .age(entity.getAge())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .profileImg(entity.getProfileImg())
                .roles(roles)
                .build();
    }
}
