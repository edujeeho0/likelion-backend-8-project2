package com.example.market.ncp.config;

import com.example.market.ncp.service.NcpGeolocationService;
import com.example.market.ncp.service.NcpMapApiService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Configuration
public class NcpClientConfig {
    private static final String NCP_APIGW_KEY_ID = "X-NCP-APIGW-API-KEY-ID";
    private static final String NCP_APIGW_KEY = "X-NCP-APIGW-API-KEY";


    @Value("${ncp.api.client-id}")
    private String ncpApiClientId;

    @Value("${ncp.api.client-secret}")
    private String ncpApiClientSecret;

    @Bean
    public RestClient ncpMapClient() {
        return RestClient.builder()
                .baseUrl("https://naveropenapi.apigw.ntruss.com")
                .defaultHeader(NCP_APIGW_KEY_ID, ncpApiClientId)
                .defaultHeader(NCP_APIGW_KEY, ncpApiClientSecret)
                .build();
    }

    @Bean
    public NcpMapApiService mapApiService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(ncpMapClient()))
                .build()
                .createClient(NcpMapApiService.class);
    }



    @Value("${ncp.api.api-access}")
    private String ncpApiAccess;

    @Value("${ncp.api.api-secret}")
    private String ncpApiSecret;

    @Bean
    public RestClient ncpGeoLocateClient() {
        return RestClient.builder()
                .baseUrl("https://geolocation.apigw.ntruss.com/geolocation/v2/geoLocation")
                .requestInitializer(request -> {
                    HttpHeaders requestHeaders = request.getHeaders();
                    Long timestamp = System.currentTimeMillis();

                    requestHeaders.add("x-ncp-apigw-timestamp", String.valueOf(timestamp));
                    requestHeaders.add("x-ncp-iam-access-key", ncpApiAccess);
                    requestHeaders.add("x-ncp-apigw-signature-v2", makeSignature(
                            request.getMethod(),
                            request.getURI().getPath() + '?' + request.getURI().getQuery(),
                            timestamp
                    ));
                })
                .build();
    }

    @Bean
    public NcpGeolocationService geolocationService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(ncpGeoLocateClient()))
                .build()
                .createClient(NcpGeolocationService.class);
    }

    public String makeSignature(HttpMethod method, String url, Long timestamp) {
        try {
            String space = " ";
            String newLine = "\n";
            String methodString = method.name();

            String message = new StringBuilder()
                    .append(methodString)
                    .append(space)
                    .append(url)
                    .append(newLine)
                    .append(timestamp)
                    .append(newLine)
                    .append(ncpApiAccess)
                    .toString();

            SecretKeySpec signingKey = new SecretKeySpec(ncpApiSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));

            return Base64.encodeBase64String(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
