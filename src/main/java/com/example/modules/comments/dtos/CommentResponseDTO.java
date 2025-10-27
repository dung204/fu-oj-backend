package com.example.modules.comments.dtos;

import com.example.base.dtos.EntityDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
// TODO: After adding fields, uncomment these two lines
// @NoArgsConstructor
// @AllArgsConstructor
public class CommentResponseDTO extends EntityDTO {

  private String userId;
  private String exerciseId;
  private String parentId;
  private String content;
}
