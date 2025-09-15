package com.example.modules.posts.dtos;

import com.example.base.dtos.EntityDTO;
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
public class PostResponseDTO extends EntityDTO {

  @Schema(description = "The title of the post", example = "Title")
  private String title;

  @Schema(description = "The content of the post", example = "Content")
  private String content;

  @Schema(description = "The author of the post")
  private UserProfileDTO user;

  @Schema(description = "Whether the post is public or not")
  private Boolean isPublic;
}
