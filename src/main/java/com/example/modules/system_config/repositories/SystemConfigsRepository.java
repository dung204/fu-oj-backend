package com.example.modules.system_config.repositories;

import com.example.modules.system_config.entities.SystemConfigs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigsRepository
  extends JpaRepository<SystemConfigs, String>, JpaSpecificationExecutor<SystemConfigs> {
  SystemConfigs findSystemConfigsById(String id);
}
