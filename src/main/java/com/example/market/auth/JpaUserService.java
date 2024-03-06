package com.example.market.auth;

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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
//@RequiredArgsConstructor
public class JpaUserService implements UserDetailsService {
    private final AuthenticationFacade authFacade;
    private final UserRepo userRepo;
    private final UserUpgradeRepo userUpgradeRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtils jwtTokenUtils;

    public JpaUserService(
            AuthenticationFacade authFacade,
            UserRepo userRepo,
            UserUpgradeRepo userUpgradeRepo, PasswordEncoder passwordEncoder,
            JwtTokenUtils jwtTokenUtils
    ) {
        this.authFacade = authFacade;
        this.userRepo = userRepo;
        this.userUpgradeRepo = userUpgradeRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtils = jwtTokenUtils;
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
    public void createUser(CreateUserDto dto) {
        if (!dto.getPassword().equals(dto.getPasswordCheck()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        if (userRepo.existsByUsername(dto.getUsername()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        userRepo.save(UserEntity.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .roles("ROLE_INACTIVE")
                .build());
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

    public void updateUser(UpdateUserDto dto){
        UserEntity userEntity = authFacade.extractUser();
        userEntity.setAge(dto.getAge());
        userEntity.setPhone(dto.getPhone());
        userEntity.setEmail(dto.getEmail());
        userEntity.setRoles("ROLE_ACTIVE");
        userRepo.save(userEntity);
    }

    public void upgradeRoleRequest(RequestUpgradeDto dto) {
        UserEntity userEntity = authFacade.extractUser();
        userUpgradeRepo.save(UserUpgrade.builder()
                .target(userEntity)
                .registrationNum(dto.getRegistrationNum())
                .build()
        );
    }
}
