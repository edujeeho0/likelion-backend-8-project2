package com.example.market.auth;

import com.example.market.auth.dto.CaptchaDto;
import com.example.market.auth.entity.Captcha;
import com.example.market.auth.exception.UserNeedsCaptchaException;
import com.example.market.auth.repo.CaptchaRepo;
import com.example.market.ncp.dto.NcpCaptchaDto;
import com.example.market.ncp.service.NcpCaptchaService;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class CaptchaHandler {
    @Value("${ncp.api.client-id}")
    private String ncpApiClientId;
    private static final String NCP_APIGW_KEY_ID = "X-NCP-APIGW-API-KEY-ID";
    private final CaptchaRepo captchaRepo;
    private final NcpCaptchaService captchaService;
    private final Gson gson;

    @ExceptionHandler(UserNeedsCaptchaException.class)
    public CaptchaDto sendCaptcha(UserNeedsCaptchaException exception) {
        Map<String, Object> params = Map.of("code", 0);
        NcpCaptchaDto ncpCaptchaDto = gson.fromJson(captchaService.captcha(params), NcpCaptchaDto.class);
        String imageUrl = UriComponentsBuilder.fromHttpUrl("https://naveropenapi.apigw.ntruss.com/captcha-bin/v1/ncaptcha")
                .queryParam("key", ncpCaptchaDto.getKey())
                .queryParam(NCP_APIGW_KEY_ID, ncpApiClientId)
                .toUriString();
        captchaRepo.save(Captcha.builder()
                .user(exception.getUser())
                .key(ncpCaptchaDto.getKey())
                .build());
        return CaptchaDto.builder()
                .key(ncpCaptchaDto.getKey())
                .imageUrl(imageUrl)
                .build();
    }
}
