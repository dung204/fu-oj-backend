package com.example.modules.users.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StudentsSearchDTO extends PaginatedQueryDTO {

  @Parameter(description = "Search for students by their full name, roll number, or email.")
  private String query;
}
