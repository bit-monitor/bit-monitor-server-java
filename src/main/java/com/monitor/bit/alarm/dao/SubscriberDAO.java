package com.monitor.bit.alarm.dao;

import com.monitor.bit.alarm.entity.SubscriberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubscriberDAO extends JpaRepository<SubscriberEntity, Long> {

     void deleteAllByAlarmId(Long alarmId);

     List<SubscriberEntity> getAllByAlarmId(Long alarmId);

     Optional<SubscriberEntity> findById(Long id);
}
