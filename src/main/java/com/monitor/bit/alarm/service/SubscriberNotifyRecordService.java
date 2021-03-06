package com.monitor.bit.alarm.service;

import com.monitor.bit.alarm.dao.SubscriberNotifyRecordDAO;
import com.monitor.bit.alarm.dto.SubscriberNotifyRecordDTO;
import com.monitor.bit.alarm.entity.AlarmEntity;
import com.monitor.bit.alarm.entity.SubscriberEntity;
import com.monitor.bit.alarm.entity.SubscriberNotifyRecordEntity;
import com.monitor.bit.alarm.vo.SubscriberNotifyRecordRelatedInfoVO;
import com.monitor.bit.alarm.vo.SubscriberNotifyRecordVO;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SubscriberNotifyRecordService extends ServiceBase {

    @Autowired
    SubscriberNotifyRecordDAO subscriberNotifyRecordDAO;
    @Autowired
    SubscriberService subscriberService;
    @Autowired
    AlarmService alarmService;

    /**
     * 新增
     *
     * @param dto dto
     * @return boolean
     */
    public boolean add(SubscriberNotifyRecordDTO dto) {
        SubscriberNotifyRecordEntity entity = new SubscriberNotifyRecordEntity();

        // alarmRecordId
        entity.setAlarmRecordId(dto.getAlarmRecordId());

        // subscriberId
        entity.setSubscriberId(dto.getSubscriberId());

        // state
        entity.setState(dto.getState());

        // content
        String content = dto.getContent();
        if (content == null) {
            content = "";
        }
        entity.setContent(content);

        // createTime
        Date createTime = new Date();
        entity.setCreateTime(createTime);

        subscriberNotifyRecordDAO.save(entity);
        return true;
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
        Long alarmRecordId = DataConvertUtils.strToLong(request.getParameter("alarmRecordId"));
        Long subscriberId = DataConvertUtils.strToLong(request.getParameter("subscriberId"));
        Integer state = DataConvertUtils.strToIntegerOrNull(request.getParameter("state"));
        String content = request.getParameter("content");

        // 拼接sql，分页查询
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSqlBuilder = new StringBuilder("select * from ams_subscriber_notify_record t where 1=1");
        StringBuilder countSqlBuilder = new StringBuilder("select count(t.id) from ams_subscriber_notify_record t where 1=1");
        StringBuilder paramSqlBuilder = new StringBuilder();

        // 报警记录id
        if (alarmRecordId != null) {
            paramSqlBuilder.append(" and t.alarm_record_id = :alarmRecordId");
            paramMap.put("alarmRecordId", alarmRecordId);
        }

        // 报警订阅方id
        if (subscriberId != null) {
            paramSqlBuilder.append(" and t.subscriber_id = :subscriberId");
            paramMap.put("subscriberId", subscriberId);
        }

        // 通知状态，0-失败，1-成功
        if (state != null) {
            paramSqlBuilder.append(" and t.state = :state");
            paramMap.put("state", state);
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

        if (!StringUtils.isEmpty(content)) {
            paramSqlBuilder.append(" and t.content like :content");
            paramMap.put("content", "%" + content + "%");
        }
        dataSqlBuilder.append(paramSqlBuilder).append(" order by t.create_time desc");
        countSqlBuilder.append(paramSqlBuilder);
        Page<SubscriberNotifyRecordEntity> page = this.findPageBySqlAndParam(SubscriberNotifyRecordEntity.class, dataSqlBuilder.toString(), countSqlBuilder.toString(), pageable, paramMap);

        // 返回
        List<SubscriberNotifyRecordVO> voList = page.getContent().stream().map(this::transEntityToVO).collect(Collectors.toList());
        PageResultBase<SubscriberNotifyRecordVO> pageResultBase = new PageResultBase<>();
        pageResultBase.setTotalNum(page.getTotalElements());
        pageResultBase.setTotalPage(page.getTotalPages());
        pageResultBase.setPageNum(pageNum);
        pageResultBase.setPageSize(pageSize);
        pageResultBase.setRecords(voList);
        return pageResultBase;
    }

    /**
     * 查询-带关联信息（预警名称）
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
        Long alarmRecordId = DataConvertUtils.strToLong(request.getParameter("alarmRecordId"));
        Long subscriberId = DataConvertUtils.strToLong(request.getParameter("subscriberId"));
        Integer state = DataConvertUtils.strToIntegerOrNull(request.getParameter("state"));
        String content = request.getParameter("content");

        // 拼接sql，分页查询
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSqlBuilder = new StringBuilder("select * from ams_subscriber_notify_record t where 1=1");
        StringBuilder countSqlBuilder = new StringBuilder("select count(t.id) from ams_subscriber_notify_record t where 1=1");
        StringBuilder paramSqlBuilder = new StringBuilder();

        // 报警记录id
        if (alarmRecordId != null) {
            paramSqlBuilder.append(" and t.alarm_record_id = :alarmRecordId");
            paramMap.put("alarmRecordId", alarmRecordId);
        }

        // 报警订阅方id
        if (subscriberId != null) {
            paramSqlBuilder.append(" and t.subscriber_id = :subscriberId");
            paramMap.put("subscriberId", subscriberId);
        }

        // 通知状态，0-失败，1-成功
        if (state != null) {
            paramSqlBuilder.append(" and t.state = :state");
            paramMap.put("state", state);
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

        if (!StringUtils.isEmpty(content)) {
            paramSqlBuilder.append(" and t.content like :content");
            paramMap.put("content", "%" + content + "%");
        }
        dataSqlBuilder.append(paramSqlBuilder).append(" order by t.create_time desc");
        countSqlBuilder.append(paramSqlBuilder);
        Page<SubscriberNotifyRecordEntity> page = this.findPageBySqlAndParam(SubscriberNotifyRecordEntity.class, dataSqlBuilder.toString(), countSqlBuilder.toString(), pageable, paramMap);

        // 返回
        List<SubscriberNotifyRecordRelatedInfoVO> voList = page.getContent().stream().map(this::transEntityToVOWithRelatedInfo).collect(Collectors.toList());
        PageResultBase<SubscriberNotifyRecordRelatedInfoVO> pageResultBase = new PageResultBase<>();
        pageResultBase.setTotalNum(page.getTotalElements());
        pageResultBase.setTotalPage(page.getTotalPages());
        pageResultBase.setPageNum(pageNum);
        pageResultBase.setPageSize(pageSize);
        pageResultBase.setRecords(voList);
        return pageResultBase;
    }

    /**
     * 根据alarmRecordId获取所有记录
     *
     * @param alarmRecordId alarmRecordId
     * @return List
     */
    public List<SubscriberNotifyRecordEntity> findAllByAlarmRecordId(long alarmRecordId) {
        return subscriberNotifyRecordDAO.findAllByAlarmRecordId(alarmRecordId);
    }

    /**
     * Entity转VO
     *
     * @param subscriberNotifyRecordEntity subscriberNotifyRecordEntity
     * @return SubscriberNotifyRecordVO
     */
    private SubscriberNotifyRecordVO transEntityToVO(SubscriberNotifyRecordEntity subscriberNotifyRecordEntity) {
        SubscriberNotifyRecordVO subscriberNotifyRecordVO = new SubscriberNotifyRecordVO();
        BeanUtils.copyProperties(subscriberNotifyRecordEntity, subscriberNotifyRecordVO);

        Long subscriberId = subscriberNotifyRecordEntity.getSubscriberId();
        SubscriberEntity subscriberEntity = subscriberService.findOneById(subscriberId).orElse(null);
        if (subscriberEntity != null) {
            subscriberNotifyRecordVO.setCategory(subscriberEntity.getCategory());
        }

        return subscriberNotifyRecordVO;
    }

    /**
     * Entity转VO-带关联信息（预警名称）
     *
     * @param subscriberNotifyRecordEntity subscriberNotifyRecordEntity
     * @return SubscriberNotifyRecordVO
     */
    private SubscriberNotifyRecordRelatedInfoVO transEntityToVOWithRelatedInfo(SubscriberNotifyRecordEntity subscriberNotifyRecordEntity) {
        SubscriberNotifyRecordRelatedInfoVO subscriberNotifyRecordRelatedInfoVO = new SubscriberNotifyRecordRelatedInfoVO();
        BeanUtils.copyProperties(subscriberNotifyRecordEntity, subscriberNotifyRecordRelatedInfoVO);

        Long subscriberId = subscriberNotifyRecordEntity.getSubscriberId();
        SubscriberEntity subscriberEntity = subscriberService.findOneById(subscriberId).orElse(null);
        if (subscriberEntity != null) {
            subscriberNotifyRecordRelatedInfoVO.setCategory(subscriberEntity.getCategory());

            long alarmId = subscriberEntity.getAlarmId();
            AlarmEntity alarmEntity = alarmService.getEntityById(alarmId);
            if (alarmEntity != null) {
                subscriberNotifyRecordRelatedInfoVO.setAlarmName(alarmEntity.getName());
            }
        }

        return subscriberNotifyRecordRelatedInfoVO;
    }
}
