package com.example.market.auth.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_table")
public class UserEntity extends BaseEntity {
    @Column(unique = true)
    private String username;
    private String password;
    private Integer age;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phone;
    private String profileImg;
    private String roles;
}
