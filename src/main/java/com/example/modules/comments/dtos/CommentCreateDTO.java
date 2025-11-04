package com.example.modules.comments.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentCreateDTO {

  @Schema(
    description = "The ID of the parent comment if this is a reply. Omit for a top-level comment."
  )
  String parentId;

  @Schema(description = "The content of the comment.")
  @NotNull
  @NotEmpty
  String content;
}
