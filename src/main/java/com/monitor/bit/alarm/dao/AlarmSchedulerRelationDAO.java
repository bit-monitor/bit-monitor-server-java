package com.monitor.bit.alarm.dao;

import com.monitor.bit.alarm.entity.AlarmSchedulerRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmSchedulerRelationDAO extends JpaRepository<AlarmSchedulerRelationEntity, Long> {

    List<AlarmSchedulerRelationEntity> findAllByAlarmId(long alarmId);
}
