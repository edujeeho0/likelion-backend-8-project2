package com.example.market.ncp.dto;

import lombok.Data;

@Data
public class NcpCaptchaDto {
    private String key;
    private Boolean result;
    private String errorCode;
    private String errorMessage;
}
