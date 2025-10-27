package com.example.modules.comments.dtos;

import com.example.base.dtos.PaginatedQueryDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentQueryDTO extends PaginatedQueryDTO {

  private String parentId;
  private String exerciseId;
}
