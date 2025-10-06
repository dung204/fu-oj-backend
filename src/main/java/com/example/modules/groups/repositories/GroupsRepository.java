package com.example.modules.groups.repositories;

import com.example.modules.groups.entities.Group;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupsRepository
  extends JpaRepository<Group, String>, JpaSpecificationExecutor<Group> {
  boolean existsGroupByCode(String code);

  Optional<Group> existsGroupByName(String name);

  @Query("SELECT g FROM Group g WHERE g.instructor.id = :ownerId AND g.deletedTimestamp == null")
  List<Group> getGroupByOwnerId(@Param("ownerId") String ownerId);

  Group getGroupById(String id);
}
