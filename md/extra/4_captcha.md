# 사업자 자동 로그인 방지

NCP 캡차를 이용해서 사업자 사용자의 로그인을 방지한다.
NCP Captcha는 NCP 지도와 같은 Application을 등록함으로서 사용 가능하며,
같은 `RestClient`도 사용 가능하다. 이를 이용해 `HttpService`를 만든다.

```java
public interface NcpCaptchaService {
    @GetExchange("/captcha/v1/nkey")
    String captcha(@RequestParam Map<String, Object> params);
}
```

```java
@Slf4j
@Configuration
public class NcpClientConfig {
    // ...
    @Bean
    public NcpCaptchaService captchaService() {
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(ncpOpenClient()))
                .build()
                .createClient(NcpCaptchaService.class);
    }
}
```

사업자 또는 관리자 사용자가 로그인을 시도할때 특정 예외를 발생시키면, `@ExceptionHandler`나 `@ControllerAdvice`를
통해 Captcha 인증을 요구하는 응답을 발생시킬 수 있다.

```java
public class UserService implements UserDetailsService {
    // ...

    public JwtResponseDto signin(JwtRequestDto dto) {
        UserEntity userEntity = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(
                dto.getPassword(),
                userEntity.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        if (userEntity.getRoles().contains("ROLE_OWNER") || userEntity.getRoles().contains("ROLE_ADMIN"))
            throw new UserNeedsCaptchaException(userEntity);
        String jwt = jwtTokenUtils.generateToken(MarketUserDetails.fromEntity(userEntity));
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response;
    }
}
```
이때 NCP Captcha의 응답 Content-Type이 `application/json`이 아니기 때문에,
Gson으로 수동으로 역직렬화 해준다.
```java
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
```

로그인을 시도한 사용자가 Captcha인증이 필요하다는 의미로 `Captcha` 엔티티를 생성하고,
이후 저장된 Key를 바탕으로 사용자가 전달한 Key와 Value에 따라 Captcha에 성공했는지 여부를
판단할 수 있다. 성공하면 JWT를 발행하면 된다.

```java
public class UserService implements UserDetailsService {
    // ...
    public JwtResponseDto captcha(CaptchaDto dto) {
        Captcha captcha = captchaRepo.findByKey(dto.getKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        captcha.setUsed(true);
        captcha = captchaRepo.save(captcha);
        Map<String, Object> params = Map.of(
                "code", 1,
                "key", captcha.getKey(),
                "value", dto.getValue());
        NcpCaptchaDto ncpCaptchaDto
                = gson.fromJson(captchaService.captcha(params), NcpCaptchaDto.class);
        if (!ncpCaptchaDto.getResult())
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        String jwt = jwtTokenUtils.generateToken(MarketUserDetails.fromEntity(captcha.getUser()));
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response;
    }
}
```
