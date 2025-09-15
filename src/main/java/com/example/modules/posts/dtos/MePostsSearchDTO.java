package com.example.modules.posts.dtos;

import com.example.base.annotations.OrderParam;
import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import java.util.Collections;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MePostsSearchDTO extends PaginatedQueryDTO {

  @Parameter(
    description = "The sorting for the query. The syntax is `{field}:{order}`. Allowed fields are: `createdTimestamp`, `updatedTimestamp`, `deletedTimestamp`, `name`."
  )
  private List<
    @OrderParam(
      allowedFields = { "createdTimestamp", "updatedTimestamp", "deletedTimestamp", "name" }
    ) String
  > order = Collections.emptyList();

  @Parameter(description = "Every posts whose names contain this name will be returned")
  private String name;
}
