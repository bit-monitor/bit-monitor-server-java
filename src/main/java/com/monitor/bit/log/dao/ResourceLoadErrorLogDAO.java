package com.monitor.bit.log.dao;

import com.monitor.bit.log.entity.ResourceLoadErrorLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ResourceLoadErrorLogDAO extends JpaRepository<ResourceLoadErrorLogEntity, Long> {

    @Query(value = "select count(id) from lms_resource_load_error_log t where t.project_identifier=?1 and t.create_time between ?2 and ?3", nativeQuery = true)
    int getCountByIdBetweenStartTimeAndEndTime(String projectIdentifier, Date startTime, Date endTime);

    @Query(value = "select date_format(create_time, '%Y-%m-%d %H') as hour, count(id) as count from lms_resource_load_error_log " +
            "where project_identifier=?3 and create_time between ?1 and ?2 group by hour", nativeQuery = true)
    List<Map<String, Object>> getLogCountByHours(Date startTime, Date endTime, String projectIdentifier);

    @Query(value = "select date_format(create_time, '%Y-%m-%d') as day, count(id) as count from lms_resource_load_error_log " +
            "where project_identifier=?3 and create_time between ?1 and date_add(?2, interval 1 day) group by day", nativeQuery = true)
    List<Map<String, Object>> getLogCountByDays(Date startTime, Date endTime, String projectIdentifier);

    @Query(value = "select count(id) as affectCounts, count(distinct page_url) as affectPages, count(distinct c_uuid) as affectUsers " +
            "from lms_resource_load_error_log where project_identifier=?3 and create_time between ?1 and ?2", nativeQuery = true)
    Map<String, Object> getOverallByTimeRange(Date startTime, Date endTime, String projectIdentifier);

    @Query(value = "select id, c_uuid, create_time from lms_resource_load_error_log " +
            "where project_identifier=?1 and create_time between ?2 and ?3", nativeQuery = true)
    List<Map<String, Object>> getLogListByCreateTimeAndProjectIdentifier(String projectIdentifier, Date startTime, Date endTime);

    @Query(value = "select * from lms_resource_load_error_log where project_identifier=?1 and create_time between ?2 and ?3", nativeQuery = true)
    List<Map<String, Object>> findAllByProjectIdentifierAndCreateTimeBetween(String projectIdentifier, Date startTime, Date endTime);

    int countDistinctCUuidByCreateTimeBetween(Date startTime, Date endTime);

    int countByCreateTimeBetween(Date startTime, Date endTime);
}

