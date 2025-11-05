package com.example.modules.comments.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentQueryDTO extends PaginatedQueryDTO {

  @Parameter(
    description = "Filter for replies to a specific comment by providing its parent ID, `null` for top-level comments"
  )
  private String parentId;

  @Parameter(description = "Filter comments belonging to a specific exercise by providing its ID.")
  private String exerciseId;
}
