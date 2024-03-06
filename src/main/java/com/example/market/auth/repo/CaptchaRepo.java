package com.example.market.auth.repo;

import com.example.market.auth.entity.Captcha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaptchaRepo extends JpaRepository<Captcha, Long> {
    Optional<Captcha> findByKey(String key);
}
