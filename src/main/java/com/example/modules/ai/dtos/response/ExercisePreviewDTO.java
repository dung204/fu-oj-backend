package com.example.modules.ai.dtos.response;

import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExercisePreviewDTO {

  private String code;
  private String title;
  private String description;
  private String solution;
  private String difficulty; // EASY, MEDIUM, HARD
  private Double timeLimit;
  private Double memory;
  private Integer maxSubmissions;
  private List<String> topicIds;
  private String visibility; // DRAFT, PUBLIC, PRIVATE
  private List<TestCaseRequestDTO> testCases;
}
