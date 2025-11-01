package com.example.modules.system_config.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SystemConfigsUpdateDTO {

  private Double easy;
  private Double medium;
  private Double difficult;
  private Double bonusTheFirstSubmit;
  private Double bonusNoWrongAnswer;
  private Double bonusTime;
  private Double countReport;
}
