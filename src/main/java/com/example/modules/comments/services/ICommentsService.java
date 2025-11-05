package com.example.modules.comments.services;

import com.example.modules.comments.dtos.CommentByExerciseQueryDTO;
import com.example.modules.comments.dtos.CommentCreateDTO;
import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.dtos.CommentUpdateDTO;
import com.example.modules.comments.entities.Comment;
import com.example.modules.users.entities.User;
import org.springframework.data.domain.Page;

public interface ICommentsService {
  public CommentResponseDTO createComment(
    String exerciseId,
    CommentCreateDTO commentRequestDTO,
    User currentUser
  );

  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    CommentQueryDTO commentQueryDTO,
    User currentUser
  );

  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    String exerciseId,
    CommentByExerciseQueryDTO commentByExerciseQueryDTO,
    User currentUser
  );

  public Comment getCommentById(String commentId, User currentUser);

  public CommentResponseDTO deleteCommentById(String commentId, User currentUser);

  public CommentResponseDTO updateCommentById(
    String commentId,
    CommentUpdateDTO commentUpdateDTO,
    User currentUser
  );

  public CommentResponseDTO reportCommentById(String commentId, int count, User currentUser);
}
