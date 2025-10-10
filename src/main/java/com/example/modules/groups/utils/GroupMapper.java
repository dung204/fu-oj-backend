package com.example.modules.groups.utils;

import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.entities.Group;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class GroupMapper {

  public abstract GroupResponseDTO toGroupResponseDTO(Group group);
}
