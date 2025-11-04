package com.example.modules.comments.services;

import com.example.modules.comments.dtos.CommentByExerciseQueryDTO;
import com.example.modules.comments.dtos.CommentCreateDTO;
import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.users.entities.User;
import org.springframework.data.domain.Page;

public interface ICommentsService {
  public CommentResponseDTO createComment(
    String exerciseId,
    CommentCreateDTO commentRequestDTO,
    User currentUser
  );

  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    CommentQueryDTO commentQueryDTO
  );

  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    String exerciseId,
    CommentByExerciseQueryDTO commentByExerciseQueryDTO
  );

  public CommentResponseDTO getCommentById(String commentId);

  public CommentResponseDTO deleteCommentById(String commentId);

  public CommentResponseDTO updateCommentById(String commentId, String content);

  public CommentResponseDTO reportCommentById(String commentId, int count);
}
