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
import com.example.modules.comments.publishers.CommentEventPublisher;
import com.example.modules.comments.repositories.CommentsRepository;
import com.example.modules.comments.utils.CommentMapper;
import com.example.modules.comments.utils.CommentsSpecification;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.services.ExercisesService;
import com.example.modules.groups.entities.Group;
import com.example.modules.redis.event_type.comment.CommentEvent;
import com.example.modules.redis.event_type.comment.CommentEventType;
import com.example.modules.system_config.entities.SystemConfigs;
import com.example.modules.system_config.repositories.SystemConfigsRepository;
import com.example.modules.users.entities.User;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentsService implements ICommentsService {

  CommentsRepository commentsRepository;
  ExercisesService exercisesService;
  CommentMapper commentMapper;
  CommentEventPublisher commentPublisher;
  SystemConfigsRepository systemConfigsRepository;

  @Override
  @SuppressWarnings("unchecked")
  public Comment getCommentById(String commentId, User currentUser) {
    Optional<Comment> comment = Optional.ofNullable(null);

    switch (currentUser.getAccount().getRole()) {
      case Role.ADMIN:
        comment = commentsRepository.findOne(
          CommentsSpecification.builder().withId(commentId).build()
        );
        break;
      case Role.INSTRUCTOR:
        comment = commentsRepository.findOne(
          CommentsSpecification.builder()
            .or(CommentsSpecification::withPublicExercises, spec ->
              spec.withExercisesCreatedBy(commentId)
            )
            .withNonDeletedExercisesOnly()
            .withId(commentId)
            .notDeleted()
            .build()
        );
        break;
      case Role.STUDENT:
        List<String> joinedGroupIds = currentUser.getJoinedGroups() == null
          ? Collections.emptyList()
          : currentUser.getJoinedGroups().stream().map(Group::getId).toList();

        comment = commentsRepository.findOne(
          CommentsSpecification.builder()
            .or(CommentsSpecification::withPublicExercises, spec ->
              spec.withExercisesOfGroups(joinedGroupIds)
            )
            .withNonDeletedExercisesOnly()
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
    Exercise exercise = exercisesService.getExerciseById(exerciseId, currentUser);

    // find parent by id
    Comment parentComment = null;
    if (commentRequestDTO.getParentId() != null) {
      parentComment = getCommentById(commentRequestDTO.getParentId(), currentUser);
    }

    // process
    Comment commentCreate = Comment.builder()
      .user(currentUser)
      .exercise(exercise)
      .parent(parentComment)
      .content(commentRequestDTO.getContent())
      .build();
    commentsRepository.save(commentCreate);
    CommentResponseDTO dto = commentMapper.toCommentResponseDTO(commentCreate);

    // publish event to redis
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
    CommentQueryDTO commentQueryDTO,
    User currentUser
  ) {
    Exercise exercise = exercisesService.getExerciseById(
      commentQueryDTO.getExerciseId(),
      currentUser
    );

    // get comments send to redis
    commentPublisher.publishCommentEvent(
      CommentEvent.builder()
        .type(CommentEventType.READ)
        .exerciseId(exercise.getId())
        .parentId(commentQueryDTO.getParentId())
        .commentId(null)
        .data(null)
        .timestamp(System.currentTimeMillis())
        .build()
    );

    return commentsRepository
      .findAll(
        CommentsSpecification.builder()
          .withParentId(commentQueryDTO.getParentId())
          .withExerciseId(exercise.getId())
          .<CommentsSpecification>conditionally(
            currentUser.getAccount().getRole() != Role.ADMIN,
            CommentsSpecification::notDeleted
          )
          .build(),
        commentQueryDTO.toPageRequest()
      )
      .map(commentMapper::toCommentResponseDTO);
  }

  @Override
  public Page<CommentResponseDTO> getCommentsByParentIdAndExerciseId(
    String exerciseId,
    CommentByExerciseQueryDTO commentByExerciseQueryDTO,
    User currentUser
  ) {
    Exercise exercise = exercisesService.getExerciseById(exerciseId, currentUser);

    return commentsRepository
      .findAll(
        CommentsSpecification.builder()
          .withParentId(commentByExerciseQueryDTO.getParentId())
          .withExerciseId(exercise.getId())
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

    // publish event to redis
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

    // publish event to redis
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

    double requiredReports = systemConfigsRepository
      .findAll()
      .stream()
      .map(SystemConfigs::getCountReport)
      .filter(value -> value != null && value > 0)
      .findFirst()
      .orElse(1000d);

    boolean reachedThreshold = requiredReports > 0 && comment.getCountReport() >= requiredReports;

    if (reachedThreshold) {
      comment.softDelete();
    }

    commentsRepository.save(comment);
    CommentResponseDTO dto = commentMapper.toCommentResponseDTO(comment);

    if (reachedThreshold) {
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
