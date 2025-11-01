package com.example.modules.exams.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.modules.exams.enums.Status;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import java.time.Instant;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponseDTO extends EntityDTO {

  private String id;
  private String code;
  private String title;
  private String description;
  private Status status;
  private Instant startTime;
  private Instant endTime;
  private String groupId;
  private String groupName;

  @Builder.Default
  private List<ExerciseResponseDTO> exercises = List.of();
}
