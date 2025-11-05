package com.example.modules.comments.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.comments.dtos.CommentByExerciseQueryDTO;
import com.example.modules.comments.dtos.CommentCreateDTO;
import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.dtos.CommentUpdateDTO;
import com.example.modules.comments.entities.Comment;
import com.example.modules.comments.exceptions.CommentNotFoundException;
import com.example.modules.comments.exceptions.CommentOperationNotAllowedException;
import com.example.modules.comments.repositories.CommentsRepository;
import com.example.modules.comments.utils.CommentMapper;
import com.example.modules.comments.utils.CommentsSpecification;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.entities.Group;
import com.example.modules.redis.configs.publishers.CommentPublisher;
import com.example.modules.redis.event_type.comment.CommentEvent;
import com.example.modules.redis.event_type.comment.CommentEventType;
import com.example.modules.system_config.entities.SystemConfigs;
import com.example.modules.system_config.repositories.SystemConfigsRepository;
import com.example.modules.users.entities.User;
import java.util.Optional;
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
  private final SystemConfigsRepository systemConfigsRepository;

  @Override
  public Comment getCommentById(String commentId, User currentUser) {
    Optional<Comment> comment = Optional.ofNullable(null);

    switch (currentUser.getAccount().getRole()) {
      case Role.ADMIN:
        comment = commentsRepository.findOne(
          CommentsSpecification.builder().withId(commentId).notDeleted().build()
        );
        break;
      case Role.INSTRUCTOR:
        comment = commentsRepository.findOne(
          CommentsSpecification.builder()
            .<CommentsSpecification>or(CommentsSpecification::withPublicExercises, spec ->
              spec.withExercisesCreatedBy(commentId)
            )
            .withId(commentId)
            .notDeleted()
            .build()
        );
        break;
      case Role.STUDENT:
        comment = commentsRepository.findOne(
          CommentsSpecification.builder()
            .withExercisesOfGroups(
              currentUser.getJoinedGroups().stream().map(Group::getId).toList()
            )
            .withId(commentId)
            .notDeleted()
            .build()
        );
        break;
    }

    return comment.orElseThrow(CommentNotFoundException::new);
  }

  @Override
  public CommentResponseDTO createComment(
    String exerciseId,
    CommentCreateDTO commentRequestDTO,
    User currentUser
  ) {
    //find exercise by id
    Exercise exercise = exercisesRepository.findExerciseById(exerciseId);

    // find parent by id
    Comment parentComment = null;
    if (commentRequestDTO.getParentId() != null) {
      parentComment = getCommentById(commentRequestDTO.getParentId(), currentUser);
    }

    //process
    Comment commentCreate = Comment.builder()
      .user(currentUser)
      .exercise(exercise)
      .parent(parentComment)
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
  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    String exerciseId,
    CommentByExerciseQueryDTO commentByExerciseQueryDTO
  ) {
    return commentsRepository
      .findAll(
        CommentsSpecification.builder()
          .withParentId(commentByExerciseQueryDTO.getParentId())
          .withExerciseId(exerciseId)
          .notDeleted()
          .build(),
        commentByExerciseQueryDTO.toPageRequest()
      )
      .map(commentMapper::toCommentResponseDTO);
  }

  @Override
  public CommentResponseDTO deleteCommentById(String commentId, User currentUser) {
    Comment comment = getCommentById(commentId, currentUser);

    if (!comment.getUser().getId().equals(currentUser.getId())) {
      throw new CommentOperationNotAllowedException();
    }

    comment.softDelete();
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
  public CommentResponseDTO updateCommentById(
    String commentId,
    CommentUpdateDTO commentUpdateDTO,
    User currentUser
  ) {
    Comment comment = getCommentById(commentId, currentUser);

    if (!comment.getUser().getId().equals(currentUser.getId())) {
      throw new CommentOperationNotAllowedException();
    }

    comment.setContent(commentUpdateDTO.getContent());
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
  public CommentResponseDTO reportCommentById(String commentId, int countReport, User currentUser) {
    Comment comment = getCommentById(commentId, currentUser);
    comment.setCountReport(countReport + 1);
    SystemConfigs systemConfigs = systemConfigsRepository.findAll().getFirst();

    if (comment.getCountReport() == Integer.parseInt("" + systemConfigs.getCountReport())) {
      comment.softDelete();
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
