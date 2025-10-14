package com.example.modules.exercises.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.test_cases.dtos.TestCaseResponseDTO;
import com.example.modules.topics.dtos.TopicResponseDTO;
import java.util.List;
import lombok.*;
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

  // default empty list to avoid null pointer exception
  @Builder.Default
  private List<TopicResponseDTO> topics = List.of();

  @Builder.Default
  private List<TestCaseResponseDTO> testCases = List.of();

  private Integer testCasesCount;
}
