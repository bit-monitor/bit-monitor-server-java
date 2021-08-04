package com.monitor.bit.alarm.dao;

import com.monitor.bit.alarm.entity.AlarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmDAO extends JpaRepository<AlarmEntity, Long> {

    Optional<AlarmEntity> getById(Long id);
}
