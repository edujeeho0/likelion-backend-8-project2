package com.example.market.auth;

import com.example.market.FileHandlerUtils;
import com.example.market.auth.dto.*;
import com.example.market.auth.entity.MarketUserDetails;
import com.example.market.auth.entity.UserEntity;
import com.example.market.auth.entity.UserUpgrade;
import com.example.market.auth.jwt.JwtTokenUtils;
import com.example.market.auth.repo.UserRepo;
import com.example.market.auth.repo.UserUpgradeRepo;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
//@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final AuthenticationFacade authFacade;
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;
    private final FileHandlerUtils fileHandlerUtils;

    public UserService(
            AuthenticationFacade authFacade,
            UserRepo userRepo,
            UserUpgradeRepo userUpgradeRepo, PasswordEncoder passwordEncoder,
            JwtTokenUtils jwtTokenUtils,
            FileHandlerUtils fileHandlerUtils) {
        this.authFacade = authFacade;
        this.userRepo = userRepo;
        this.userUpgradeRepo = userUpgradeRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtils = jwtTokenUtils;
        this.fileHandlerUtils = fileHandlerUtils;
        userRepo.saveAll(List.of(
                UserEntity.builder()
                        .username("inactive")
                        .password(passwordEncoder.encode("test"))
                        .roles("ROLE_INACTIVE")
                        .build(),
                UserEntity.builder()
                        .username("normal")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("normal@gmail.com")
                        .phone("01012345678")
                        .roles("ROLE_ACTIVE")
                        .build(),
                UserEntity.builder()
                        .username("normal2")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("normal2@gmail.com")
                        .phone("01012345679")
                        .roles("ROLE_ACTIVE")
                        .build(),
                UserEntity.builder()
                        .username("shop_owner")
                        .password(passwordEncoder.encode("test"))
                        .age(30)
                        .email("owner@gmail.com")
                        .phone("01087654321")
                        .roles("ROLE_ACTIVE")
                        .roles("ROLE_ACTIVE,ROLE_OWNER")
                        .build(),
                UserEntity.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("test"))
                        .roles("ROLE_ACTIVE,ROLE_ADMIN")
                        .build()
        ));
    }

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
                .roles("ROLE_INACTIVE")
                .build()));
    }

    public JwtResponseDto signin(JwtRequestDto dto) {
        UserEntity userEntity = userRepo.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(
                dto.getPassword(),
                userEntity.getPassword()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        String jwt = jwtTokenUtils.generateToken(MarketUserDetails.fromEntity(userEntity));
        JwtResponseDto response = new JwtResponseDto();
        response.setToken(jwt);
        return response;
    }

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
        )
                userEntity.setRoles("ROLE_ACTIVE");
        return UserDto.fromEntity(userRepo.save(userEntity));
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