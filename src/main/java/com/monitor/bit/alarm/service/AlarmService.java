package com.monitor.bit.alarm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.monitor.bit.alarm.dto.AlarmDTO;
import com.monitor.bit.alarm.dto.AlarmSchedulerRelationDTO;
import com.monitor.bit.alarm.entity.AlarmSchedulerRelationEntity;
import com.monitor.bit.alarm.entity.SubscriberEntity;
import com.monitor.bit.alarm.scheduler.AlarmScheduler;
import com.monitor.bit.alarm.vo.AlarmVO;
import com.monitor.bit.auth.service.TokenService;
import com.monitor.bit.common.api.PageResultBase;
import com.monitor.bit.project.entity.ProjectEntity;
import com.monitor.bit.project.service.ProjectService;
import com.monitor.bit.schedule.dto.SchedulerDTO;
import com.monitor.bit.schedule.entity.SchedulerEntity;
import com.monitor.bit.schedule.service.SchedulerService;
import com.monitor.bit.utils.DataConvertUtils;
import com.monitor.bit.alarm.dao.AlarmDAO;
import com.monitor.bit.alarm.entity.AlarmEntity;
import com.monitor.bit.common.service.ServiceBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AlarmService extends ServiceBase {

    @Autowired
    private AlarmDAO alarmDao;
    @Autowired
    private SubscriberService subscriberService;
    @Autowired
    private AlarmScheduler alarmScheduler;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private SchedulerService schedulerService;
    @Autowired
    private AlarmSchedulerRelationService alarmSchedulerRelationService;

    /**
     * ??????
     *
     * @param alarmDTO alarmDTO
     * @param request  request
     * @return Object
     * @throws Exception Exception
     */
    @Transactional(rollbackOn = {Exception.class})
    public Object add(AlarmDTO alarmDTO, HttpServletRequest request) throws Exception {

        // ???DTO???????????????
        AlarmEntity alarmEntity = new AlarmEntity();
        BeanUtils.copyProperties(alarmDTO, alarmEntity);

        // ???????????????????????????
        Date nowTime = new Date();
        alarmEntity.setCreateTime(nowTime);
        alarmEntity.setUpdateTime(nowTime);
        // createBy
        Long createBy = tokenService.getUserIdByRequest(request);
        if (createBy == null) {
            throw new Exception("token?????????");
        }
        alarmEntity.setCreateBy(createBy);
        // isDeleted
        alarmEntity.setIsDeleted(0);

        alarmDao.save(alarmEntity);

        String subscriberList = alarmDTO.getSubscriberList();
        List<HashMap<String, Object>> list = DataConvertUtils.jsonStrToObject(subscriberList, List.class);
        for (HashMap<String, Object> map : list) {
            SubscriberEntity subscriberEntity = DataConvertUtils.mapToBean(map, SubscriberEntity.class);
            if (subscriberEntity == null) throw new Exception("subscriberList???????????????");
            subscriberEntity.setAlarmId(alarmEntity.getId());
            subscriberService.add(subscriberEntity);
        }

        if (alarmEntity.getIsActive() == 1) {
            // ????????????????????????
            this.startAlarmScheduler(alarmEntity);
        }

        return alarmEntity;
    }

    /**
     * ??????
     *
     * @param request request
     * @return Object
     */
    @Transactional(rollbackOn = {Exception.class})
    public Object update(HttpServletRequest request) throws Exception {

        // ????????????
        Long id = DataConvertUtils.strToLong(request.getParameter("id"));
        if (id == null) {
            throw new Exception("id????????????");
        }

        // ????????????
        String name = request.getParameter("name");
        String projectIdentifier = request.getParameter("projectIdentifier");
        Integer level = DataConvertUtils.strToIntegerOrNull(request.getParameter("level"));
        Integer category = DataConvertUtils.strToIntegerOrNull(request.getParameter("category"));
        String rule = request.getParameter("rule");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        Integer silentPeriod = DataConvertUtils.strToIntegerOrNull(request.getParameter("silentPeriod"));
        Integer isActive = DataConvertUtils.strToIntegerOrNull(request.getParameter("isActive"));
        Long createBy = DataConvertUtils.strToLong(request.getParameter("createBy"));
        Integer isDeleted = DataConvertUtils.strToIntegerOrNull(request.getParameter("isDeleted"));
        String subscriberList = request.getParameter("subscriberList");

        // ??????
        AlarmEntity alarmEntity = alarmDao.getById(id).orElseThrow(() -> new Exception("??????????????????"));
        if (!StringUtils.isEmpty(name)) {
            alarmEntity.setName(name);
        }
        if (projectIdentifier != null && !projectIdentifier.isEmpty()) {
            alarmEntity.setProjectIdentifier(projectIdentifier);
        }
        if (level != null) {
            alarmEntity.setLevel(level);
        }
        if (category != null) {
            alarmEntity.setCategory(category);
        }
        if (!StringUtils.isEmpty(rule)) {
            alarmEntity.setRule(rule);
        }
        if (!StringUtils.isEmpty(startTime)) {
            alarmEntity.setStartTime(startTime);
        }
        if (!StringUtils.isEmpty(endTime)) {
            alarmEntity.setEndTime(endTime);
        }
        if (silentPeriod != null) {
            alarmEntity.setSilentPeriod(silentPeriod);
        }
        if (isActive != null) {

            // ????????????????????????????????????????????????????????????????????????????????????????????????
            this.stopAlarmScheduler(alarmEntity);
            if (isActive == 1) {
                this.startAlarmScheduler(alarmEntity);
            }

            alarmEntity.setIsActive(isActive);
        }
        if (createBy != null) {
            alarmEntity.setCreateBy(createBy);
        }
        if (isDeleted != null) {
            alarmEntity.setIsDeleted(isDeleted);
        }
        if (!StringUtils.isEmpty(subscriberList)) {

            // ??????????????????????????????
            subscriberService.deleteAllByAlarmId(alarmEntity.getId());

            // ???????????????????????????
            List<HashMap<String, Object>> list = DataConvertUtils.jsonStrToObject(subscriberList, List.class);
            for (HashMap<String, Object> map : list) {
                SubscriberEntity subscriberEntity = DataConvertUtils.mapToBean(map, SubscriberEntity.class);
                if (subscriberEntity == null) throw new Exception("subscriberList???????????????");
                subscriberEntity.setAlarmId(alarmEntity.getId());
                subscriberService.add(subscriberEntity);
            }
        }

        // ????????????
        alarmDao.save(alarmEntity);

        return alarmEntity;
    }

    /**
     * ??????
     *
     * @param request request
     * @return Object
     */
    public Object get(HttpServletRequest request) {

        // ??????????????????
        int pageNum = DataConvertUtils.strToInt(request.getParameter("pageNum"));
        int pageSize = DataConvertUtils.strToInt(request.getParameter("pageSize"));
        String name = request.getParameter("name");
        String projectIdentifier = request.getParameter("projectIdentifier");
        Integer level = DataConvertUtils.strToIntegerOrNull(request.getParameter("level"));
        Integer category = DataConvertUtils.strToIntegerOrNull(request.getParameter("category"));
        String rule = request.getParameter("rule");
        String startTime = request.getParameter("startTime");
        String endTime = request.getParameter("endTime");
        Integer silentPeriod = DataConvertUtils.strToIntegerOrNull(request.getParameter("silentPeriod"));
        Integer isActive = DataConvertUtils.strToIntegerOrNull(request.getParameter("isActive"));
        Long createBy = DataConvertUtils.strToLong(request.getParameter("createBy"));
        Integer isDeleted = DataConvertUtils.strToIntegerOrNull(request.getParameter("isDeleted"));

        // ??????sql???????????????
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        Map<String, Object> paramMap = new HashMap<>();
        StringBuilder dataSqlBuilder = new StringBuilder("select * from ams_alarm t where 1=1");
        StringBuilder countSqlBuilder = new StringBuilder("select count(t.id) from ams_alarm t where 1=1");
        StringBuilder paramSqlBuilder = new StringBuilder();

        // ????????????
        if (!StringUtils.isEmpty(name)) {
            paramSqlBuilder.append(" and t.name like :name");
            paramMap.put("name", "%" + name + "%");
        }
        // ????????????
        if (!StringUtils.isEmpty(projectIdentifier)) {
            paramSqlBuilder.append(" and t.project_identifier = :projectIdentifier");
            paramMap.put("projectIdentifier", projectIdentifier);
        }
        // ????????????
        if (level != null) {
            paramSqlBuilder.append(" and t.level = :level");
            paramMap.put("level", level);
        }
        // ????????????
        if (category != null) {
            paramSqlBuilder.append(" and t.category = :category");
            paramMap.put("category", category);
        }
        // ????????????
        if (!StringUtils.isEmpty(rule)) {
            paramSqlBuilder.append(" and t.rule like :rule");
            paramMap.put("rule", "%" + rule + "%");
        }
        // ????????????-????????????
        if (!StringUtils.isEmpty(startTime)) {
            paramSqlBuilder.append(" and t.start_time = :startTime");
            paramMap.put("startTime", startTime);
        }
        // ????????????-????????????
        if (!StringUtils.isEmpty(endTime)) {
            paramSqlBuilder.append(" and t.end_time = :endTime");
            paramMap.put("endTime", endTime);
        }
        // ?????????
        if (silentPeriod != null) {
            paramSqlBuilder.append(" and t.silent_period = :silentPeriod");
            paramMap.put("silentPeriod", silentPeriod);
        }
        // ????????????
        if (isActive != null) {
            paramSqlBuilder.append(" and t.is_active = :isActive");
            paramMap.put("isActive", isActive);
        }
        // ?????????ID
        if (createBy != null) {
            paramSqlBuilder.append(" and t.create_by = :createBy");
            paramMap.put("createBy", createBy);
        }
        // ??????????????????
        if (isDeleted != null) {
            paramSqlBuilder.append(" and t.is_deleted = :isDeleted");
            paramMap.put("isDeleted", isDeleted);
        }
        dataSqlBuilder.append(paramSqlBuilder).append(" order by t.update_time desc");
        countSqlBuilder.append(paramSqlBuilder);
        Page<AlarmEntity> page = this.findPageBySqlAndParam(AlarmEntity.class, dataSqlBuilder.toString(), countSqlBuilder.toString(), pageable, paramMap);

        // ??????
        List<AlarmVO> voList = page.getContent().stream().map(this::transAlarmEntityToAlarmVO).collect(Collectors.toList());
        PageResultBase<AlarmVO> pageResultBase = new PageResultBase<>();
        pageResultBase.setTotalNum(page.getTotalElements());
        pageResultBase.setTotalPage(page.getTotalPages());
        pageResultBase.setPageNum(pageNum);
        pageResultBase.setPageSize(pageSize);
        pageResultBase.setRecords(voList);
        return pageResultBase;
    }

    /**
     * ??????id??????????????????
     *
     * @param id id
     * @return AlarmEntity
     */
    public AlarmEntity getEntityById(long id) {
        return alarmDao.getById(id).orElse(null);
    }

    /**
     * ??????
     *
     * @param id id
     * @return Object
     */
    @Transactional(rollbackOn = {Exception.class})
    public Object delete(Long id) throws Exception {
        AlarmEntity alarmEntity = alarmDao.findById(id).orElseThrow(() -> new Exception("???????????????"));

        // ????????????????????????
        subscriberService.deleteAllByAlarmId(id);

        // ????????????????????????
        this.stopAlarmScheduler(alarmEntity);

        // ??????????????????
        alarmDao.deleteById(id);

        return true;
    }

    /**
     * ??????alarmId???????????????????????????
     *
     * @param alarmId alarmId
     * @return String
     */
    public String getProjectNameByAlarmId(long alarmId) {
        String projectName = "";
        AlarmEntity alarmEntity = alarmDao.getById(alarmId).orElse(null);
        if (alarmEntity != null) {
            String projectIdentifier = alarmEntity.getProjectIdentifier();
            ProjectEntity projectEntity = projectService.findByProjectIdentifier(projectIdentifier).orElse(null);
            if (projectEntity != null) {
                projectName = projectEntity.getProjectName();
            }
        }
        return projectName;
    }

    /**
     * AlarmEntity???AlarmVO
     *
     * @param alarmEntity alarmEntity
     * @return AlarmVO
     */
    private AlarmVO transAlarmEntityToAlarmVO(AlarmEntity alarmEntity) {
        AlarmVO alarmVO = new AlarmVO();
        BeanUtils.copyProperties(alarmEntity, alarmVO);
        try {
            this.setSubscriberListToAlarmVO(alarmVO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alarmVO;
    }

    /**
     * ??????subscriberList
     *
     * @param alarmVO alarmVO
     */
    private void setSubscriberListToAlarmVO(AlarmVO alarmVO) throws JsonProcessingException {
        Long alarmId = alarmVO.getId();
        List<SubscriberEntity> subscriberEntityList = subscriberService.getAllByAlarmId(alarmId);
        String subscriberList = "";
        if (subscriberEntityList.size() > 0) {
            subscriberList = DataConvertUtils.objectToJsonStr(subscriberEntityList);
        }
        alarmVO.setSubscriberList(subscriberList);
    }

    /**
     * ????????????????????????
     *
     * @param alarmEntity alarmEntity
     */
    @Transactional(rollbackOn = {Exception.class})
    void startAlarmScheduler(AlarmEntity alarmEntity) {
        String params = null;
        try {
            params = DataConvertUtils.objectToJsonStr(alarmEntity);
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        // ??????????????????
        SchedulerDTO schedulerDTO = new SchedulerDTO();
        String beanName = alarmScheduler.getBeanName();
        String methodName = alarmScheduler.getMethodName();
        String cronExpression = alarmScheduler.getCronExpression();
        schedulerDTO.setBeanName(beanName);
        schedulerDTO.setMethodName(methodName);
        schedulerDTO.setParams(params);
        schedulerDTO.setCronExpression(cronExpression);
        schedulerDTO.setState(1);
        SchedulerEntity schedulerEntity = schedulerService.add(schedulerDTO);

        // ????????????-?????????????????????
        Long alarmId = alarmEntity.getId();
        Long schedulerId = schedulerEntity.getId();
        AlarmSchedulerRelationDTO alarmSchedulerRelationDTO = new AlarmSchedulerRelationDTO();
        alarmSchedulerRelationDTO.setAlarmId(alarmId);
        alarmSchedulerRelationDTO.setSchedulerId(schedulerId);
        alarmSchedulerRelationService.add(alarmSchedulerRelationDTO);

        // ??????????????????
        schedulerService.startScheduler(schedulerEntity);
    }

    /**
     * ????????????????????????
     *
     * @param alarmEntity alarmEntity
     */
    void stopAlarmScheduler(AlarmEntity alarmEntity) {

        int isActive = alarmEntity.getIsActive();
        if (isActive == 0) return;

        // ???????????????????????????
        long alarmId = alarmEntity.getId();
        List<AlarmSchedulerRelationEntity> list = alarmSchedulerRelationService.findAllByAlarmId(alarmId);
        if (list.size() == 0) return;

        list.forEach(entity -> {
            long schedulerId = entity.getSchedulerId();
            SchedulerEntity schedulerEntity = schedulerService.getById(schedulerId).orElse(null);
            if (schedulerEntity == null) return;

            // ??????????????????????????????????????????
            schedulerService.stopScheduler(schedulerEntity);

            // ??????????????????
            schedulerService.deleteByEntity(schedulerEntity);

            // ????????????-?????????????????????
            alarmSchedulerRelationService.deleteByEntity(entity);
        });
    }
}
