package com.example.modules.comments.services;

import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentRequestDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.entities.Comment;
import com.example.modules.comments.repositories.CommentsRepository;
import com.example.modules.comments.utils.CommentMapper;
import com.example.modules.comments.utils.CommentsSpecification;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.redis.configs.publishers.CommentPublisher;
import com.example.modules.redis.event_type.comment.CommentEvent;
import com.example.modules.redis.event_type.comment.CommentEventType;
import com.example.modules.users.entities.User;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentsService implements ICommentsService {

  private final CommentsRepository commentsRepository;
  private final ExercisesRepository exercisesRepository;
  private final CommentMapper commentMapper;
  private final CommentPublisher commentPublisher;

  @Override
  public CommentResponseDTO createComment(User user, CommentRequestDTO commentRequestDTO) {
    //find exercise by id
    Exercise exercise = exercisesRepository.findExerciseById(commentRequestDTO.getExerciseId());
    // find parent by id
    Comment comment = null;
    if (commentRequestDTO.getParentId() != null) {
      comment = commentsRepository.findCommentById(commentRequestDTO.getParentId());
    }
    //process
    Comment commentCreate = Comment.builder()
      .user(user)
      .exercise(exercise)
      .parent(comment)
      .content(commentRequestDTO.getContent())
      .build();
    commentsRepository.save(commentCreate);
    CommentResponseDTO dto = commentMapper.toCommentResponseDTO(commentCreate);

    //publish event to redis
    commentPublisher.publishCommentEvent(
      CommentEvent.builder()
        .type(CommentEventType.CREATED)
        .exerciseId(dto.getExerciseId())
        .parentId(dto.getParentId())
        .commentId(dto.getId())
        .data(dto)
        .timestamp(System.currentTimeMillis())
        .build()
    );
    return dto;
  }

  @Override
  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    CommentQueryDTO commentQueryDTO
  ) {
    return commentsRepository
      .findAll(
        CommentsSpecification.builder()
          .withParentId(commentQueryDTO.getParentId())
          .withExerciseId(commentQueryDTO.getExerciseId())
          .notDeleted()
          .build(),
        commentQueryDTO.toPageRequest()
      )
      .map(commentMapper::toCommentResponseDTO);
  }

  @Override
  public CommentResponseDTO deleteCommentById(String commentId) {
    Comment comment = commentsRepository.findCommentById(commentId);
    comment.setDeletedTimestamp(Instant.now());
    commentsRepository.save(comment);
    CommentResponseDTO dto = commentMapper.toCommentResponseDTO(comment);

    //publish event to redis
    commentPublisher.publishCommentEvent(
      CommentEvent.builder()
        .type(CommentEventType.DELETED)
        .exerciseId(dto.getExerciseId())
        .parentId(dto.getParentId())
        .commentId(dto.getId())
        .data(null)
        .timestamp(System.currentTimeMillis())
        .build()
    );
    return dto;
  }

  @Override
  public CommentResponseDTO updateCommentById(String commentId, String content) {
    Comment comment = commentsRepository.findCommentById(commentId);
    comment.setContent(content);
    comment.setUpdatedTimestamp(Instant.now());
    commentsRepository.save(comment);
    CommentResponseDTO dto = commentMapper.toCommentResponseDTO(comment);

    //publish event to redis
    commentPublisher.publishCommentEvent(
      CommentEvent.builder()
        .type(CommentEventType.UPDATED)
        .exerciseId(dto.getExerciseId())
        .parentId(dto.getParentId())
        .commentId(dto.getId())
        .data(dto)
        .timestamp(System.currentTimeMillis())
        .build()
    );
    return dto;
  }

  @Override
  public CommentResponseDTO reportCommentById(String commentId, int contReport) {
    Comment comment = commentsRepository.findCommentById(commentId);
    comment.setCountReport(contReport + 1);
    if (comment.getCountReport() == 10) {
      comment.setDeletedTimestamp(Instant.now());
      commentsRepository.save(comment);
      CommentResponseDTO dto = commentMapper.toCommentResponseDTO(comment);
      //publish event to redis
      commentPublisher.publishCommentEvent(
        CommentEvent.builder()
          .type(CommentEventType.DELETED)
          .exerciseId(dto.getExerciseId())
          .parentId(dto.getParentId())
          .commentId(dto.getId())
          .data(null)
          .timestamp(System.currentTimeMillis())
          .build()
      );
    }
    commentsRepository.save(comment);
    CommentResponseDTO dto = commentMapper.toCommentResponseDTO(comment);
    //publish event to redis
    commentPublisher.publishCommentEvent(
      CommentEvent.builder()
        .type(CommentEventType.UPDATED)
        .exerciseId(dto.getExerciseId())
        .parentId(dto.getParentId())
        .commentId(dto.getId())
        .data(dto)
        .timestamp(System.currentTimeMillis())
        .build()
    );
    return dto;
  }
}
