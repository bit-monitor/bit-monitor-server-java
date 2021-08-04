package com.monitor.bit.user.dao;

import com.monitor.bit.user.entity.UserProjectRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserProjectRelationDAO extends JpaRepository<UserProjectRelationEntity, Long> {

    List<UserProjectRelationEntity> findByUserId(Long userId);

    List<UserProjectRelationEntity> findByProjectId(Long projectId);

    void deleteByUserIdAndProjectId(Long userId, Long projectId);

    void deleteAllByProjectId(Long projectId);
}
