package com.example.market.admin.dto;

import com.example.market.auth.entity.UserUpgrade;
import lombok.*;

@Getter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserUpgradeDto {
    private Long id;
    private String username;
    private String registrationNum;
    private Boolean approved;

    public static UserUpgradeDto fromEntity(UserUpgrade entity) {
        return UserUpgradeDto.builder()
                .id(entity.getId())
                .username(entity.getTarget().getUsername())
                .registrationNum(entity.getRegistrationNum())
                .approved(entity.getApproved())
                .build();
    }
}
