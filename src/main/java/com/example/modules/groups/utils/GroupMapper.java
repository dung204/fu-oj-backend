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
  @Mapping(
    target = "aLittleStudent",
    expression = "java(group.getStudents() != null ? group.getStudents().stream().limit(5).map(user -> UserMapper.INSTANCE.toUserProfileDTO(user)).toList() : java.util.Collections.emptyList())"
  )
  @Mapping(target = "joined", ignore = true)
  public abstract GroupResponseDTO toGroupResponseDTO(Group group);
}
