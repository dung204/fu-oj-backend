package com.example.modules.groups.utils;

import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.users.utils.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = UserMapper.class)
@Slf4j
public abstract class GroupMapper {

  @Named("toGroupResponseDTO")
  @Mapping(source = "instructor", target = "owner", qualifiedByName = "toUserProfileDTO")
  @Mapping(
    target = "studentsCount",
    expression = "java(group.getStudents() != null ? group.getStudents().size() : 0)"
  )
  public abstract GroupResponseDTO toGroupResponseDTO(Group group);
}
