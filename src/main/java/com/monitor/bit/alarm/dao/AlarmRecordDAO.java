package com.monitor.bit.alarm.dao;

import com.monitor.bit.alarm.entity.AlarmRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRecordDAO extends JpaRepository<AlarmRecordEntity, Long> {

    List<AlarmRecordEntity> findAllByAlarmId(long alarmId);
}
