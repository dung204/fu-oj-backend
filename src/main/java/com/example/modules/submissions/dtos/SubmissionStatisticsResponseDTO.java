package com.example.modules.submissions.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmissionStatisticsResponseDTO {

  @JsonProperty("ACCEPTED")
  private Long accepted;

  @JsonProperty("WRONG_ANSWER")
  private Long wrongAnswer;

  @JsonProperty("TIME_LIMIT_EXCEEDED")
  private Long timeLimitExceeded;

  @JsonProperty("COMPILATION_ERROR")
  private Long compilationError;

  @JsonProperty("RUNTIME_ERROR")
  private Long runtimeError;

  @JsonProperty("MEMORY_LIMIT_EXCEEDED")
  private Long memoryLimitExceeded;

  private Long totalCount;
}
