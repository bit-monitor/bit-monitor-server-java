package com.monitor.bit.user.service;

import com.monitor.bit.auth.dto.LoginUserDTO;
import com.monitor.bit.auth.service.TokenService;
import com.monitor.bit.common.api.PageResultBase;
import com.monitor.bit.common.service.ServiceBase;
import com.monitor.bit.project.entity.ProjectEntity;
import com.monitor.bit.project.service.ProjectService;
import com.monitor.bit.user.dao.UserDAO;
import com.monitor.bit.user.dao.UserProjectRelationDAO;
import com.monitor.bit.user.dto.UserInfoDTO;
import com.monitor.bit.user.entity.UserEntity;
import com.monitor.bit.user.entity.UserProjectRelationEntity;
import com.monitor.bit.utils.DataConvertUtils;
import com.monitor.bit.utils.TokenUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserService extends ServiceBase {

    @Autowired
    private UserDAO userDao;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserProjectRelationDAO userProjectRelationDao;

    /**
     * 新增
     *
     * @param userEntity userEntity
     * @return Object
     */
    public Object add(UserEntity userEntity) {

        log.info("--------[UserService]保存开始--------");

        // 创建时间
        Date createTime = new Date();

        // 保存实体
        userEntity.setIsAdmin(0);
        userEntity.setCreateTime(createTime);
        userDao.save(userEntity);

        log.info("--------[UserService]保存结束--------");

        return userEntity;
    }

    /**
     * 多条件分页查询
     *
     * @param request request
     * @return Object
     */
    public Object get(HttpServletRequest request) throws Exception {
        String isNeedPagingParam = request.getParameter("isNeedPaging");
        if (isNeedPagingParam == null) {
            throw new Exception("isNeedPaging参数不正确");
        }
        int isNeedPaging = DataConvertUtils.strToInt(isNeedPagingParam);
        if (isNeedPaging == 0) {
            // 不需要分页
            return userDao.findAll();
        } else if (isNeedPaging == 1) {
            // 需要分页
            // 获取请求参数
            int pageNum = DataConvertUtils.strToInt(request.getParameter("pageNum"));
            int pageSize = DataConvertUtils.strToInt(request.getParameter("pageSize"));
            String username = request.getParameter("username");
            String phone = request.getParameter("phone");
            Integer gender = DataConvertUtils.strToIntegerOrNull(request.getParameter("gender"));

            // 拼接sql，分页查询
            Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
            Map<String, Object> paramMap = new HashMap<>();
            StringBuilder dataSqlBuilder = new StringBuilder("select * from ums_user t where 1=1");
            StringBuilder countSqlBuilder = new StringBuilder("select count(t.id) from ums_user t where 1=1");
            StringBuilder paramSqlBuilder = new StringBuilder();

            // 用户名
            if (!StringUtils.isEmpty(username)) {
                paramSqlBuilder.append(" and t.username like :username");
                paramMap.put("username", "%" + username + "%");
            }

            // 电话
            if (!StringUtils.isEmpty(phone)) {
                paramSqlBuilder.append(" and t.phone like :phone");
                paramMap.put("phone", "%" + phone + "%");
            }

            // 性别
            if (gender != null) {
                paramSqlBuilder.append(" and t.gender = :gender");
                paramMap.put("gender", gender);
            }
            dataSqlBuilder.append(paramSqlBuilder).append(" order by t.create_time desc");
            countSqlBuilder.append(paramSqlBuilder);
            Page<UserEntity> page = this.findPageBySqlAndParam(UserEntity.class, dataSqlBuilder.toString(), countSqlBuilder.toString(), pageable, paramMap);

            // 返回
            PageResultBase<UserEntity> pageResultBase = new PageResultBase<>();
            pageResultBase.setTotalNum(page.getTotalElements());
            pageResultBase.setTotalPage(page.getTotalPages());
            pageResultBase.setPageNum(pageNum);
            pageResultBase.setPageSize(pageSize);
            pageResultBase.setRecords(page.getContent());
            return pageResultBase;
        } else {
            throw new Exception("isNeedPaging参数不正确");
        }
    }

    /**
     * 获取用户详情
     *
     * @param request request
     * @return Object
     */
    public Object getDetail(HttpServletRequest request) throws Exception {
        Long id = tokenService.getUserIdByRequest(request);
        if (id == null) {
            throw new Exception("请求错误，用户不存在");
        } else {
            UserEntity userEntity = userDao.findById(id).orElseThrow(() -> new Exception("用户不存在"));
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            BeanUtils.copyProperties(userEntity, userInfoDTO);
            return userInfoDTO;
        }
    }

    /**
     * 登录
     *
     * @param request request
     * @return Object
     * @throws Exception Exception
     */
    public Object login(HttpServletRequest request) throws Exception {
        String username = DataConvertUtils.getStrOrEmpty(request.getParameter("username"));
        String password = DataConvertUtils.getStrOrEmpty(request.getParameter("password"));

        // 参数判断
        if (username.length() == 0 || password.length() == 0) {
            throw new Exception("用户名或密码不能为空");
        }

        // 查询
        List<UserEntity> userEntityList = userDao.findByUsernameAndPassword(username, password);
        if (userEntityList.size() == 0) {
            throw new Exception("用户名或密码不正确");
        }

        // 获取token并存入redis
        UserEntity userEntity = userEntityList.get(0);
        String token = TokenUtils.getToken();
        return tokenService.addOrUpdateToken(token, userEntity.getId(), userEntity.getUsername(), userEntity.getIsAdmin());
    }

    /**
     * 根据用户获取关联的项目
     *
     * @param request request
     * @return Object
     */
    public List<ProjectEntity> getRelatedProjectListByRequest(HttpServletRequest request) {

        // 通过获取userId
        String token = request.getHeader("token");
        LoginUserDTO user = tokenService.getUserByToken(token);

        // 获取关联的项目列表
        List<UserProjectRelationEntity> relatedProjectList = userProjectRelationDao.findByUserId(user.getId());
        List<Long> projectIdList = relatedProjectList.stream().map(UserProjectRelationEntity::getProjectId).collect(Collectors.toList());
        List<ProjectEntity> resultList = new ArrayList<>();
        for (Long projectId : projectIdList) {
            Optional<ProjectEntity> optional = projectService.getProjectById(projectId);
            ProjectEntity projectEntity = optional.orElse(null);
            if (projectEntity != null) {
                resultList.add(projectEntity);
            }
        }

        return resultList;
    }
}
