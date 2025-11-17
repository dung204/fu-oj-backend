package com.example.modules.ai.controller;

import com.example.modules.ai.dtos.request.ExerciseGenerationRequest;
import com.example.modules.ai.dtos.response.ExerciseGenerationResponse;
import com.example.modules.ai.service.ExerciseGenerationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/exercises")
@RequiredArgsConstructor
@Tag(name = "AI Exercise Generation", description = "API để tạo bài tập tự động bằng AI")
public class ExerciseGenerationController {

  private final ExerciseGenerationService exerciseGenerationService;

  @PostMapping("/generate")
  @Operation(
    summary = "Tạo bài tập từ prompt",
    description = "Nhận prompt từ giáo viên và trả về danh sách bài tập preview (chưa lưu vào database)"
  )
  public ResponseEntity<ExerciseGenerationResponse> generateExercises(
    @Valid @RequestBody ExerciseGenerationRequest request
  ) {
    ExerciseGenerationResponse response = exerciseGenerationService.generateExercises(request);
    return ResponseEntity.ok(response);
  }
}
