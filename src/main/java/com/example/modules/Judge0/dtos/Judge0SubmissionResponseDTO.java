package com.example.modules.Judge0.dtos;

import com.example.modules.submissions.dtos.Judge0StatusDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Judge0SubmissionResponseDTO {

  @JsonProperty("stdout")
  private String stdout;

  @JsonProperty("stderr")
  private String stderr;

  @JsonProperty("compile_output")
  private String compileOutput;

  @JsonProperty("message")
  private String message;

  @JsonProperty("time")
  private String time;

  @JsonProperty("memory")
  private Integer memory;

  @JsonProperty("token")
  private String token;

  @JsonProperty("status")
  private Judge0StatusDTO status;

  @JsonProperty("expected_output")
  private String expectedOutput;

  @JsonProperty("exit_code")
  private Integer exitCode;
}
