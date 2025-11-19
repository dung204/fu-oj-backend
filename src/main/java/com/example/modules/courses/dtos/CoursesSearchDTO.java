package com.example.modules.courses.dtos;

import com.example.base.annotations.OrderParam;
import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoursesSearchDTO extends PaginatedQueryDTO {

  @Parameter(
    description = "Filter courses by title. The search is case-insensitive and matches any part of the title."
  )
  private String title;

  @Parameter(
    description = "The sorting for the query. The syntax is `{field}:{order}`. Allowed fields are: `createdTimestamp`, `title`."
  )
  private List<@OrderParam(allowedFields = { "createdTimestamp", "title" }) String> order =
    Collections.emptyList();
}
