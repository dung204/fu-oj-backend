package com.example.modules.topics.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TopicsSearchDTO extends PaginatedQueryDTO {

  @Parameter(description = "Every topics whose name contain this name will be returned")
  private String name;
}
