package com.example.modules.auth.repositories;

import com.example.modules.auth.entities.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionsRepository
  extends JpaRepository<Permission, String>, JpaSpecificationExecutor<Permission> {}
