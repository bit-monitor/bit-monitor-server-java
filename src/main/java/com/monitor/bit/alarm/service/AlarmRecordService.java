package com.monitor.bit.alarm.service;

import com.monitor.bit.alarm.dao.AlarmRecordDAO;
import com.monitor.bit.alarm.dto.AlarmRecordDTO;
import com.monitor.bit.alarm.entity.AlarmEntity;
import com.monitor.bit.alarm.entity.AlarmRecordEntity;
import com.monitor.bit.alarm.entity.SubscriberNotifyRecordEntity;
import com.monitor.bit.alarm.vo.AlarmRecordVO;
import com.monitor.bit.alarm.vo.AlarmRecordWithRelatedInfoVO;
import com.monitor.bit.common.api.PageResultBase;
import com.monitor.bit.common.service.ServiceBase;
import com.monitor.bit.utils.DataConvertUtils;
import com.monitor.bit.utils.DateUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlarmRecordService extends ServiceBase {

    @Autowired
    AlarmRecordDAO alarmRecordDAO;
    @Autowired
    SubscriberNotifyRecordService subscriberNotifyRecordService;
    @Autowired
    AlarmService alarmService;

    /**
     * 新增
     *
     * @param alarmRecordDTO alarmRecordDTO
     * @return boolean
     */
    public long add(AlarmRecordDTO alarmRecordDTO) {
        AlarmRecordEntity alarmRecordEntity = new AlarmRecordEntity();

        // alarmId
        alarmRecordEntity.setAlarmId(alarmRecordDTO.getAlarmId());

        // alarmData
        alarmRecordEntity.setAlarmData(alarmRecordDTO.getAlarmData());

        // createTime
        Date createTime = new Date();
        alarmRecordEntity.setCreateTime(createTime);

        return alarmRecordDAO.save(alarmRecordEntity).getId();
    }

    /**
     * 查询
     *
     * @param request request
     * @return Object
     */
    public Object get(HttpServletRequest request) {

        // 获取请求参数
        int pageNum = DataConvertUtils.strToInt(request.getParameter("pageNum"));
        int pageSize = DataConvertUtils.strToInt(request.getParameter("pageSize"));
        Date startTime = DateUtils.strToDate(request.getParameter("startTime"), "yyyy-MM-dd HH:mm:ss");
        Date endTime = DateUtils.strToDate(request.getParameter("endTime"), "yyyy-MM-dd HH:mm:ss");
        Long alarmId = DataConvertUtils.strToLong(request.getParameter("alarmId"));
        String alarmData = request.getParameter("alarmData");

        // 拼接sql，分页查询
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSqlBuilder = new StringBuilder("select * from ams_alarm_record t where 1=1");
        StringBuilder countSqlBuilder = new StringBuilder("select count(t.id) from ams_alarm_record t where 1=1");
        StringBuilder paramSqlBuilder = new StringBuilder();

        // 预警id
        if (alarmId != null) {
            paramSqlBuilder.append(" and t.alarm_id = :alarmId");
            paramMap.put("alarmId", alarmId);
        }

        // 报警内容，格式为JSON字符串
        if (!StringUtils.isEmpty(alarmData)) {
            paramSqlBuilder.append(" and t.alarm_data like :alarmData");
            paramMap.put("alarmData", "%" + alarmData + "%");
        }

        // 开始时间、结束时间
        if (startTime != null && endTime != null) {
            paramSqlBuilder.append(" and t.create_time between :startTime and :endTime");
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
        } else if (startTime != null) {
            paramSqlBuilder.append(" and t.create_time >= :startTime");
            paramMap.put("startTime", startTime);
        } else if (endTime != null) {
            paramSqlBuilder.append(" and t.create_time <= :endTime");
            paramMap.put("endTime", endTime);
        }
        dataSqlBuilder.append(paramSqlBuilder).append(" order by t.create_time desc");
        countSqlBuilder.append(paramSqlBuilder);
        Page<AlarmRecordEntity> page = this.findPageBySqlAndParam(AlarmRecordEntity.class, dataSqlBuilder.toString(), countSqlBuilder.toString(), pageable, paramMap);

        // 返回
        List<AlarmRecordVO> voList = page.getContent().stream().map(this::transEntityToVO).collect(Collectors.toList());
        PageResultBase<AlarmRecordVO> pageResultBase = new PageResultBase<>();
        pageResultBase.setTotalNum(page.getTotalElements());
        pageResultBase.setTotalPage(page.getTotalPages());
        pageResultBase.setPageNum(pageNum);
        pageResultBase.setPageSize(pageSize);
        pageResultBase.setRecords(voList);
        return pageResultBase;
    }

    /**
     * 查询-返回关联的信息（预警名称）
     *
     * @param request request
     * @return Object
     */
    public Object getWithRelatedInfo(HttpServletRequest request) {

        // 获取请求参数
        int pageNum = DataConvertUtils.strToInt(request.getParameter("pageNum"));
        int pageSize = DataConvertUtils.strToInt(request.getParameter("pageSize"));
        Date startTime = DateUtils.strToDate(request.getParameter("startTime"), "yyyy-MM-dd HH:mm:ss");
        Date endTime = DateUtils.strToDate(request.getParameter("endTime"), "yyyy-MM-dd HH:mm:ss");
        Long alarmId = DataConvertUtils.strToLong(request.getParameter("alarmId"));
        String alarmData = request.getParameter("alarmData");

        // 拼接sql，分页查询
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSqlBuilder = new StringBuilder("select * from ams_alarm_record t where 1=1");
        StringBuilder countSqlBuilder = new StringBuilder("select count(t.id) from ams_alarm_record t where 1=1");
        StringBuilder paramSqlBuilder = new StringBuilder();

        // 预警id
        if (alarmId != null) {
            paramSqlBuilder.append(" and t.alarm_id = :alarmId");
            paramMap.put("alarmId", alarmId);
        }

        // 报警内容，格式为JSON字符串
        if (!StringUtils.isEmpty(alarmData)) {
            paramSqlBuilder.append(" and t.alarm_data like :alarmData");
            paramMap.put("alarmData", "%" + alarmData + "%");
        }

        // 开始时间、结束时间
        if (startTime != null && endTime != null) {
            paramSqlBuilder.append(" and t.create_time between :startTime and :endTime");
            paramMap.put("startTime", startTime);
            paramMap.put("endTime", endTime);
        } else if (startTime != null) {
            paramSqlBuilder.append(" and t.create_time >= :startTime");
            paramMap.put("startTime", startTime);
        } else if (endTime != null) {
            paramSqlBuilder.append(" and t.create_time <= :endTime");
            paramMap.put("endTime", endTime);
        }
        dataSqlBuilder.append(paramSqlBuilder).append(" order by t.create_time desc");
        countSqlBuilder.append(paramSqlBuilder);
        Page<AlarmRecordEntity> page = this.findPageBySqlAndParam(AlarmRecordEntity.class, dataSqlBuilder.toString(), countSqlBuilder.toString(), pageable, paramMap);

        // 返回
        List<AlarmRecordWithRelatedInfoVO> voList = page.getContent().stream().map(this::transEntityToVOWithRelatedInfo).collect(Collectors.toList());
        PageResultBase<AlarmRecordWithRelatedInfoVO> pageResultBase = new PageResultBase<>();
        pageResultBase.setTotalNum(page.getTotalElements());
        pageResultBase.setTotalPage(page.getTotalPages());
        pageResultBase.setPageNum(pageNum);
        pageResultBase.setPageSize(pageSize);
        pageResultBase.setRecords(voList);
        return pageResultBase;
    }

    /**
     * Entity转VO
     *
     * @param alarmRecordEntity alarmRecordEntity
     * @return AlarmRecordVO
     */
    private AlarmRecordVO transEntityToVO(AlarmRecordEntity alarmRecordEntity) {
        AlarmRecordVO alarmRecordVO = new AlarmRecordVO();
        BeanUtils.copyProperties(alarmRecordEntity, alarmRecordVO);

        return alarmRecordVO;
    }

    /**
     * Entity转VO，带关联的信息（预警名称）
     *
     * @param alarmRecordEntity alarmRecordEntity
     * @return AlarmRecordWithRelatedInfoVO
     */
    private AlarmRecordWithRelatedInfoVO transEntityToVOWithRelatedInfo(AlarmRecordEntity alarmRecordEntity) {
        AlarmRecordWithRelatedInfoVO alarmRecordWithRelatedInfoVO = new AlarmRecordWithRelatedInfoVO();
        BeanUtils.copyProperties(alarmRecordEntity, alarmRecordWithRelatedInfoVO);

        // 获取关联的预警名称
        long alarmId = alarmRecordEntity.getAlarmId();
        AlarmEntity alarmEntity = alarmService.getEntityById(alarmId);
        if (alarmEntity != null) {
            alarmRecordWithRelatedInfoVO.setAlarmName(alarmEntity.getName());
        }

        return alarmRecordWithRelatedInfoVO;
    }
}
