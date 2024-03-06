package com.example.market.auth.entity;

import com.example.market.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

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
    @Setter
    private Integer age;
    @Setter
    @Column(unique = true)
    private String email;
    @Setter
    @Column(unique = true)
    private String phone;
    @Setter
    private String profileImg;
    @Setter
    private String roles;
}
