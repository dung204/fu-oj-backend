package com.example.modules.system_config.utils;

import com.example.modules.system_config.dtos.SystemConfigsResponseDTO;
import com.example.modules.system_config.dtos.SystemConfigsUpdateDTO;
import com.example.modules.system_config.entities.SystemConfigs;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class SystemConfigMapper {

  public abstract SystemConfigsResponseDTO toSystemConfigsResponseDTO(SystemConfigs systemConfigs);

  public abstract SystemConfigs toEntity(SystemConfigsUpdateDTO dto);
}
