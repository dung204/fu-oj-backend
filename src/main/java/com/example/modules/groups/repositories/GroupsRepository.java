package com.example.modules.groups.repositories;

import com.example.modules.groups.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsRepository
  extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {}
