package com.example.market.ncp.service;

import com.example.market.ncp.dto.NcpCaptchaDto;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;

import java.util.Map;

public interface NcpCaptchaService {
    @GetExchange("/captcha/v1/nkey")
    String captcha(@RequestParam Map<String, Object> params);
}
