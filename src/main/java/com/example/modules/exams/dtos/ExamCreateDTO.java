package com.example.modules.exams.dtos;

import com.example.modules.exams.enums.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamCreateDto {

  @NotBlank(message = "Code is required")
  private String code;

  @NotBlank(message = "Title is required")
  private String title;

  @NotBlank(message = "Description is required")
  private String description;

  private Status status = Status.UPCOMING; // or whatever default value you need

  @NotNull(message = "Start time is required")
  private Instant startTime;

  @NotNull(message = "End time is required")
  private Instant endTime;

  private List<String> groupId;

  private List<String> exerciseIds;
}
