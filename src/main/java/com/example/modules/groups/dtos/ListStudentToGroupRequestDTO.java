package com.example.modules.groups.dtos;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ListStudentToGroupRequestDTO {

  @NotEmpty(message = "studentIds must not be empty")
  private List<String> studentIds;
}
