package com.badarak.infrastructure.adapter.out.persistence.repository;

import com.badarak.domain.model.UserStatus;
import com.badarak.infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {
    boolean existsByEmail(String email);
    Page<UserEntity> findAll(Pageable pageable);
    Page<UserEntity> findAllByStatus(UserStatus status, Pageable pageable);
}
