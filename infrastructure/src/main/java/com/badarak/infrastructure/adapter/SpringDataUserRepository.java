package com.badarak.infrastructure.adapter;

import com.badarak.infrastructure.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {}
