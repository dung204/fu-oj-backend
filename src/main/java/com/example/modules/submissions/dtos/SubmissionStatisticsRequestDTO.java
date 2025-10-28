package com.example.modules.submissions.dtos;

import com.example.modules.submissions.enums.Verdict;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import lombok.Data;

@Data
public class SubmissionStatisticsRequestDTO {

  @Parameter(
    description = "Every submissions whose status is one of these statuses will be returned",
    array = @ArraySchema(schema = @Schema(implementation = Verdict.class))
  )
  private Set<String> status;

  @Parameter(
    description = "Every submissions whose languageCode is one of these languageCode will be returned"
  )
  private Set<String> languageCode;

  @Parameter(description = "Every submissions of this student will be returned")
  private String student;

  @Parameter(description = "Every submissions of this exercise will be returned")
  private String exercise;
}
