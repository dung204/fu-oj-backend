package com.example.modules.ai.dtos.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseGenerationResponse {

  private List<ExercisePreviewDTO> exercises;
}
