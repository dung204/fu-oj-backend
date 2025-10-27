package com.example.modules.comments.services;

import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentRequestDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.users.entities.User;
import org.springframework.data.domain.Page;

public interface ICommentsService {
  public CommentResponseDTO createComment(User user, CommentRequestDTO commentRequestDTO);

  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    CommentQueryDTO commentQueryDTO
  );

  public CommentResponseDTO deleteCommentById(String commentId);

  public CommentResponseDTO updateCommentById(String commentId, String content);

  public CommentResponseDTO reportCommentById(String commentId, int count);
}
