package com.example.modules.system_config.services;

import com.example.modules.system_config.dtos.SystemConfigsResponseDTO;
import com.example.modules.system_config.dtos.SystemConfigsUpdateDTO;

public interface ISystemConfigsService {
  public SystemConfigsResponseDTO getSystemConfigs();

  public SystemConfigsResponseDTO updateSystemConfigs(
    SystemConfigsUpdateDTO systemConfigsUpdateDTO,
    String id
  );
}
