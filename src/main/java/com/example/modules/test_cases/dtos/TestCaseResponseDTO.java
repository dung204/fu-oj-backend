package com.example.modules.test_cases.dtos;

import com.example.base.dtos.EntityDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TestCaseResponseDTO extends EntityDTO {

  private String input;
  private String output;
  private Boolean isPublic;
}
