package com.monitor.bit.user.dao;

import com.monitor.bit.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserDAO extends JpaRepository<UserEntity, Long> {

    List<UserEntity> findByUsernameAndPassword(String username, String password);

    Optional<UserEntity> findById(Long id);
}
