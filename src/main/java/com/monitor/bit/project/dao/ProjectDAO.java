package com.monitor.bit.project.dao;

import com.monitor.bit.project.entity.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectDAO extends JpaRepository<ProjectEntity, Long> {
    Optional<ProjectEntity> findById(Long id);

    Optional<ProjectEntity> findByProjectIdentifier(String projectIdentifier);
}
