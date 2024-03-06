package com.example.market.auth.repo;

import com.example.market.auth.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ValidationRepo extends JpaRepository<Validation, Long> {
    Optional<Validation> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
