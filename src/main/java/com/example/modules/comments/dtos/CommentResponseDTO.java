package com.example.modules.comments.dtos;

import com.example.base.dtos.EntityDTO;
import com.example.base.utils.SwaggerExamples;
import com.example.modules.users.dtos.UserProfileDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO extends EntityDTO {

  private UserProfileDTO user;

  @Schema(example = SwaggerExamples.UUID)
  private String exerciseId;

  @Schema(example = SwaggerExamples.UUID)
  private String parentId;

  private String content;
}
