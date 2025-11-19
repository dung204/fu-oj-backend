package com.example.modules.ai.dtos.request;

import com.example.base.annotations.AllowedStrings;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExerciseGenerationRequest {

  @Min(value = 1, message = "Number of exercises must be at least 1")
  Integer numberOfExercise;

  @NotEmpty(message = "At least one difficulty level is required")
  List<String> level; // ["EASY", "MEDIUM", "HARD"]

  @NotBlank(message = "Topic is required")
  String topic; // topic ID

  @Min(value = 1, message = "Number of public test cases must be at least 1")
  Integer numberOfPublicTestCases; // Số test case hiện (public)

  @Min(value = 1, message = "Number of private test cases must be at least 1")
  Integer numberOfPrivateTestCases; // Số test case ẩn (private)

  @Min(value = 1, message = "Total test cases per exercise must be at least 1")
  Integer totalTestCasesPerExercise; // Tổng số test case mỗi bài tập

  @NotBlank(message = "Solution language is required")
  String solutionLanguage; // Ngôn ngữ cho solution (ví dụ: "Java", "Python", "C++", "C", "JavaScript")

  @AllowedStrings(values = { "DRAFT", "PRIVATE" })
  String visibility = "DRAFT"; // Visibility của bài tập: DRAFT hoặc PRIVATE

  String prompt; // Prompt tùy chỉnh (optional) - nếu có thì ưu tiên sử dụng prompt này
}
