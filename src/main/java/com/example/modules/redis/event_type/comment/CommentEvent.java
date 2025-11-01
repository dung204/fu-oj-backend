package com.example.modules.redis.event_type.comment;

import com.example.modules.comments.dtos.CommentResponseDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentEvent {

  private CommentEventType type;
  private String exerciseId;
  private String parentId; // optional
  private String commentId;
  private CommentResponseDTO data;
  private long timestamp;
}
