package com.example.modules.system_config.dtos;

import com.example.base.dtos.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
// TODO: After adding fields, uncomment these two lines
@NoArgsConstructor
@AllArgsConstructor
public class SystemConfigsResponseDTO extends EntityDTO {

  // TODO: Add fields
  private Double easy;
  private Double medium;
  private Double difficult;
  private Double bonusTheFirstSubmit;
  private Double bonusNoWrongAnswer;
  private Double bonusTime;
  private Double countReport;
}
