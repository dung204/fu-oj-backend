package com.example.modules.test_cases.dtos;

import com.example.base.annotations.OrderParam;
import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TestCaseQueryDTO extends PaginatedQueryDTO {

  @Parameter(description = "Filter by isPublic status")
  private Boolean isPublic;

  @Parameter(description = "Sorting fields. Allowed: isPublic, createdTimestamp, updatedTimestamp")
  public List<
    @OrderParam(
      allowedFields = { "isPublic", "createdTimestamp", "updatedTimestamp", "deletedTimestamp" }
    ) String
  > order = Collections.emptyList();
}
