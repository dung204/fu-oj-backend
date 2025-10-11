package com.example.modules.Judge0.dtos;

import com.example.modules.submissions.dtos.Judge0StatusDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Judge0 callback webhook request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Judge0CallbackRequestDTO {

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
}
