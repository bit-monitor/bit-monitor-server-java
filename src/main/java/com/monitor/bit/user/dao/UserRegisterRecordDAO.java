package com.monitor.bit.user.dao;

import com.monitor.bit.user.entity.UserRegisterRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRegisterRecordDAO extends JpaRepository<UserRegisterRecordEntity, Long> {
    Optional<UserRegisterRecordEntity> findById(Long id);
}
