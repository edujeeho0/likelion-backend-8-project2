package com.example.market.auth.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Captcha extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;
    private String captchaKey;
    @Setter
    @Builder.Default
    private Boolean used = false;
}
