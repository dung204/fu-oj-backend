package com.example.modules.courses.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseCreateDTO {

  @NotBlank
  private String title;

  @NotBlank
  private String description;
}
