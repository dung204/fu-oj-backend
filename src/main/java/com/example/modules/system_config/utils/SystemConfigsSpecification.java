package com.example.modules.system_config.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.system_config.entities.SystemConfigs;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemConfigsSpecification extends SpecificationBuilder<SystemConfigs> {

  public static SystemConfigsSpecification builder() {
    return new SystemConfigsSpecification();
  }
}
