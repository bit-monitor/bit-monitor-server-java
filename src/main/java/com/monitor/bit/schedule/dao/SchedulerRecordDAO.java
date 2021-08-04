package com.monitor.bit.schedule.dao;

import com.monitor.bit.schedule.entity.SchedulerRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerRecordDAO extends JpaRepository<SchedulerRecordEntity, Long> {
}
