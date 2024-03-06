package com.example.market.auth.dto;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaptchaDto {
    private String key;
    private String imageUrl;
    private String value;
}
