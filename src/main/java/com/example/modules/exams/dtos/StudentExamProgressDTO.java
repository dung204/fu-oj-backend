package com.example.modules.exams.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.*;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentExamProgressDTO {

  @Schema(description = "User ID", example = SwaggerExamples.UUID)
  private String userId;

  @Schema(description = "Student roll number", example = SwaggerExamples.ROLL_NUMBER)
  private String rollNumber;

  @Schema(description = "First name", example = SwaggerExamples.FIRST_NAME)
  private String firstName;

  @Schema(description = "Last name", example = SwaggerExamples.LAST_NAME)
  private String lastName;

  @Schema(description = "Email", example = SwaggerExamples.EMAIL)
  private String email;

  @Schema(description = "Total score from ExamRanking", example = "85.5")
  private Double totalScore;

  @Schema(description = "Whether the student has joined the exam (has ExamRanking)")
  private Boolean hasJoined;

  @Schema(description = "Whether the exam is completed (ExamRanking.completed = true)")
  private Boolean isCompleted;

  @Schema(description = "List of exercise progress for this student")
  @Builder.Default
  private List<ExerciseProgressDTO> submissionExercises = List.of();
}
