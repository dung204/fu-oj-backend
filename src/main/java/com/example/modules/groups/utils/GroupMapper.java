package com.example.modules.groups.utils;

import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.entities.Group;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.utils.UserMapper;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = UserMapper.class)
@Slf4j
public abstract class GroupMapper {

  @Autowired
  protected UserMapper userMapper;

  @Named("toGroupResponseDTO")
  @Mapping(source = "instructor", target = "owner", qualifiedByName = "toUserProfileDTO")
  @Mapping(
    target = "studentsCount",
    expression = "java(group.getStudents() != null ? group.getStudents().size() : 0)"
  )
  @Mapping(
    target = "aLittleStudent",
    expression = "java(mapStudentsToUserProfileDTOs(group.getStudents()))"
  )
  @Mapping(target = "joined", ignore = true)
  public abstract GroupResponseDTO toGroupResponseDTO(Group group);

  protected List<UserProfileDTO> mapStudentsToUserProfileDTOs(List<User> students) {
    if (students == null || students.isEmpty()) {
      return Collections.emptyList();
    }
    return students
      .stream()
      .limit(5)
      .map(userMapper::toUserProfileDTO)
      .collect(Collectors.toList());
  }
}
