package com.monitor.bit.alarm.dao;

import com.monitor.bit.alarm.entity.SubscriberNotifyRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriberNotifyRecordDAO extends JpaRepository<SubscriberNotifyRecordEntity, Long> {

    List<SubscriberNotifyRecordEntity> findAllByAlarmRecordId(long alarmRecordId);
}
