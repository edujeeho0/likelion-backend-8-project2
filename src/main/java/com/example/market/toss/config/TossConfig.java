package com.example.market.toss.config;

import com.example.market.toss.TossHttpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Configuration
public class TossConfig {
    @Value("${toss.secret}")
    private String tossSecret;

    @Bean
    public RestClient tossClient() {
        String basicAuth = Base64.getEncoder().encodeToString((tossSecret + ":").getBytes());
        return RestClient
                .builder()
                .baseUrl("https://api.tosspayments.com/v1")
                .defaultHeader("Authorization", String.format("Basic %s", basicAuth))
                .defaultStatusHandler(
                        HttpStatusCode::isError,
                        (request, response) -> {
                            log.info("{}", response.getStatusCode());
                            log.info(new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8));
                })
                .build();
    }

    @Bean
    public TossHttpService httpService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(tossClient()))
                .build()
                .createClient(TossHttpService.class);
    }
}
