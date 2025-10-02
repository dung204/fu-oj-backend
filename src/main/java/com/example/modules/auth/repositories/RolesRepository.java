package com.example.modules.auth.repositories;

import com.example.modules.auth.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RolesRepository
  extends JpaRepository<Role, String>, JpaSpecificationExecutor<Role> {}
