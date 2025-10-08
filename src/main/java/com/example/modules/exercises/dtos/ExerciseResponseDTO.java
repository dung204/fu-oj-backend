package com.example.modules.exercises.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.topics.dtos.TopicsResponseDTO;
import java.util.List;
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
public class ExerciseResponseDTO extends EntityDTO {

  private String code;
  private String title;
  private String description;
  private Integer maxSubmissions;
  private List<TopicsResponseDTO> topics;
  private List<TestCaseResponseDTO> testCases;
  private Integer testCasesCount;
}
