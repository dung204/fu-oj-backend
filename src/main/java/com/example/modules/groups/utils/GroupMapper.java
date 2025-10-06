package com.example.modules.groups.utils;

import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.entities.Group;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class GroupMapper {

  @Named("toGroupResponseDTO")
  @Mapping(source = "instructor.id", target = "ownerId")
  public abstract GroupResponseDTO toGroupResponseDTO(Group group);

  public abstract List<GroupResponseDTO> toGroupResponseDTOList(List<Group> groups);
}
