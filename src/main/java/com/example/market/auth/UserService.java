package com.example.market.auth;

import com.example.market.FileHandlerUtils;
import com.example.market.alert.AlertService;
import com.example.market.auth.dto.*;
import com.example.market.auth.entity.*;
import com.example.market.auth.exception.UserNeedsCaptchaException;
import com.example.market.auth.jwt.JwtTokenUtils;
import com.example.market.auth.repo.CaptchaRepo;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import com.example.market.auth.repo.ValidationRepo;
import com.example.market.ncp.dto.NcpCaptchaDto;
import com.example.market.ncp.service.NcpCaptchaService;
import com.google.gson.Gson;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final AuthenticationFacade authFacade;
    private final UserRepo userRepo;
    private final ValidationRepo validationRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final CaptchaRepo captchaRepo;
    private final NcpCaptchaService captchaService;
    private final AlertService alertService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    private final FileHandlerUtils fileHandlerUtils;
    private final Gson gson;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username)
                .map(MarketUserDetails::fromEntity)
                .orElseThrow(() -> new UsernameNotFoundException("not found"));
    }

    @Transactional
    public UserDto createUser(CreateUserDto dto) {
        if (!dto.getPassword().equals(dto.getPasswordCheck()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (userRepo.existsByUsername(dto.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        return UserDto.fromEntity(userRepo.save(UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .build()));
    }

    public JwtResponseDto signin(JwtRequestDto dto) {
        UserEntity userEntity = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(
                dto.getPassword(),
                userEntity.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

//        if (userEntity.getRoles().contains("ROLE_OWNER") || userEntity.getRoles().contains("ROLE_ADMIN"))
//            throw new UserNeedsCaptchaException(userEntity);
        String jwt = jwtTokenUtils.generateToken(MarketUserDetails.fromEntity(userEntity));
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response;
    }

    public JwtResponseDto captcha(CaptchaDto dto) {
        Captcha captcha = captchaRepo.findByKey(dto.getKey())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        captcha.setUsed(true);
        captcha = captchaRepo.save(captcha);
        Map<String, Object> params = Map.of(
                "code", 1,
                "key", captcha.getCaptchaKey(),
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

    @Transactional
    public UserDto updateUser(UpdateUserDto dto){
        UserEntity userEntity = authFacade.extractUser();
        userEntity.setAge(dto.getAge());
        userEntity.setPhone(dto.getPhone());
        userEntity.setEmail(dto.getEmail());
        if (
                userEntity.getAge() != null &&
                userEntity.getEmail() != null &&
                userEntity.getPhone() != null &&
                userEntity.getRoles().equals("ROLE_INACTIVE")
        ) {
            String validationCode = UUID.randomUUID().toString().split("-")[0];
            validationRepo.save(Validation.builder()
                    .user(userEntity)
                    .validation(validationCode)
                    .validated(false)
                    .build());
            alertService.sendValidation(userEntity, validationCode);
        }
        return UserDto.fromEntity(userRepo.save(userEntity));
    }

    @Transactional
    public UserDto validate(String code) {
        UserEntity user = authFacade.extractUser();
        if (!user.getRoles().equals("ROLE_INACTIVE"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        Validation validation = validationRepo.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
        if (validation.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(10)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (validation.getValidation().equals(code)) {
            user.setRoles("ROLE_ACTIVE");
            return UserDto.fromEntity(userRepo.save(user));
        } else throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public void requestValidate() {
        UserEntity userEntity = authFacade.extractUser();
        if (
                userEntity.getAge() != null &&
                userEntity.getEmail() != null &&
                userEntity.getPhone() != null &&
                userEntity.getRoles().equals("ROLE_INACTIVE")
        ) {
            String validationCode = UUID.randomUUID().toString().split("-")[0];
            validationRepo.save(Validation.builder()
                    .user(userEntity)
                    .validation(validationCode)
                    .validated(false)
                    .build());
            alertService.sendValidation(userEntity, validationCode);
        }
        else throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }

    public void upgradeRoleRequest(RequestUpgradeDto dto) {
        UserEntity target = authFacade.extractUser();
        userUpgradeRepo.save(UserUpgrade.builder()
                .target(target)
                .registrationNum(dto.getRegistrationNum())
                .build()
        );
    }

    public UserDto profileImg(MultipartFile file) {
        UserEntity userEntity = authFacade.extractUser();
        String requestPath = fileHandlerUtils.saveFile(
                String.format("users/%d/", userEntity.getId()),
                "profile",
                file
        );

        userEntity.setProfileImg(requestPath);
        return UserDto.fromEntity(userRepo.save(userEntity));
    }
}
