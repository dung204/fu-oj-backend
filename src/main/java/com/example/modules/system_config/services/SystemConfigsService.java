package com.example.modules.system_config.services;

import com.example.modules.system_config.dtos.SystemConfigsResponseDTO;
import com.example.modules.system_config.dtos.SystemConfigsUpdateDTO;
import com.example.modules.system_config.entities.SystemConfigs;
import com.example.modules.system_config.repositories.SystemConfigsRepository;
import com.example.modules.system_config.utils.SystemConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SystemConfigsService implements ISystemConfigsService {

  private final SystemConfigsRepository systemConfigsRepository;
  private final SystemConfigMapper systemConfigMapper;

  @Override
  public SystemConfigsResponseDTO getSystemConfigs() {
    SystemConfigs systemConfigs = null;
    if (systemConfigsRepository.findAll().size() == 0) {
      systemConfigs = new SystemConfigs();
      systemConfigs.setEasy(100.0);
      systemConfigs.setMedium(200.0);
      systemConfigs.setDifficult(300.0);
      systemConfigs.setBonusTheFirstSubmit(10.0);
      systemConfigs.setBonusNoWrongAnswer(5.0);
      systemConfigs.setBonusTime(10.0);
      systemConfigs.setCountReport(10.0);
      systemConfigsRepository.save(systemConfigs);
    } else {
      systemConfigs = systemConfigsRepository.findAll().getFirst();
    }

    return systemConfigMapper.toSystemConfigsResponseDTO(systemConfigs);
  }

  @Override
  public SystemConfigsResponseDTO updateSystemConfigs(
    SystemConfigsUpdateDTO systemConfigsUpdateDTO,
    String id
  ) {
    SystemConfigs systemConfigs = systemConfigsRepository.findSystemConfigsById(id);
    //set
    systemConfigs.setEasy(systemConfigsUpdateDTO.getEasy());
    systemConfigs.setMedium(systemConfigsUpdateDTO.getMedium());
    systemConfigs.setDifficult(systemConfigsUpdateDTO.getDifficult());
    systemConfigs.setBonusTheFirstSubmit(systemConfigsUpdateDTO.getBonusTheFirstSubmit());
    systemConfigs.setBonusNoWrongAnswer(systemConfigsUpdateDTO.getBonusNoWrongAnswer());
    systemConfigs.setBonusTime(systemConfigsUpdateDTO.getBonusTime());
    systemConfigs.setCountReport(systemConfigsUpdateDTO.getCountReport());
    //save
    systemConfigsRepository.save(systemConfigs);
    return systemConfigMapper.toSystemConfigsResponseDTO(systemConfigs);
  }
}
