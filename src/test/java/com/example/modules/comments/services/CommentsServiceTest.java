package com.example.modules.comments.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.enums.Role;
import com.example.modules.comments.dtos.CommentCreateDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.entities.Comment;
import com.example.modules.comments.exceptions.CommentOperationNotAllowedException;
import com.example.modules.comments.publishers.CommentEventPublisher;
import com.example.modules.comments.repositories.CommentsRepository;
import com.example.modules.comments.utils.CommentMapper;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.services.ExercisesService;
import com.example.modules.redis.event_type.comment.CommentEvent;
import com.example.modules.system_config.entities.SystemConfigs;
import com.example.modules.system_config.repositories.SystemConfigsRepository;
import com.example.modules.users.entities.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class CommentsServiceTest extends BaseServiceTest {

  @Mock
  private CommentsRepository commentsRepository;

  @Mock
  private ExercisesService exercisesService;

  @Mock
  private CommentMapper commentMapper;

  @Mock
  private CommentEventPublisher commentPublisher;

  @Mock
  private SystemConfigsRepository systemConfigsRepository;

  @InjectMocks
  private CommentsService commentsService;

  private Exercise exercise;

  @BeforeEach
  void setUp() {
    exercise = Exercise.builder()
      .id("exercise-1")
      .code("EX-1")
      .title("Two Sum")
      .description("Find pair")
      .solution("solution")
      .build();
  }

  @Test
  void getCommentById_WhenAdminRole_ShouldReturnComment() {
    User admin = getMockUser();
    admin.getAccount().setRole(Role.ADMIN);
    Comment comment = buildComment(admin);
    when(commentsRepository.findOne(any())).thenReturn(Optional.of(comment));

    Comment result = commentsService.getCommentById("comment-1", admin);

    assertEquals(comment, result);
  }

  @Test
  void createComment_WhenValid_ShouldPublishCreateEvent() {
    User student = getMockUser();
    student.getAccount().setRole(Role.STUDENT);
    CommentResponseDTO responseDTO = CommentResponseDTO.builder().exerciseId("exercise-1").build();

    when(exercisesService.getExerciseById("exercise-1", student)).thenReturn(exercise);
    when(commentsRepository.save(any(Comment.class))).thenAnswer(invocation ->
      invocation.getArgument(0)
    );
    when(commentMapper.toCommentResponseDTO(any(Comment.class))).thenReturn(responseDTO);

    CommentResponseDTO result = commentsService.createComment(
      "exercise-1",
      CommentCreateDTO.builder().content("Nice explanation").build(),
      student
    );

    assertEquals("exercise-1", result.getExerciseId());
    verify(commentPublisher).publishCommentEvent(any(CommentEvent.class));
  }

  @Test
  void deleteCommentById_WhenNotOwner_ShouldThrowException() {
    User student = getMockUser();
    student.getAccount().setRole(Role.STUDENT);
    User otherUser = getMockUser();
    otherUser.setId("other");
    Comment comment = buildComment(otherUser);
    when(commentsRepository.findOne(any())).thenReturn(Optional.of(comment));

    assertThrows(CommentOperationNotAllowedException.class, () ->
      commentsService.deleteCommentById("comment-1", student)
    );
  }

  @Test
  void reportCommentById_WhenThresholdReached_ShouldPublishDeleteEvent() {
    User admin = getMockUser();
    admin.getAccount().setRole(Role.ADMIN);
    Comment comment = buildComment(admin);
    when(commentsRepository.findOne(any())).thenReturn(Optional.of(comment));
    when(systemConfigsRepository.findAll()).thenReturn(
      List.of(SystemConfigs.builder().countReport(1d).build())
    );
    when(commentMapper.toCommentResponseDTO(comment)).thenReturn(
      CommentResponseDTO.builder().exerciseId("exercise-1").build()
    );

    commentsService.reportCommentById("comment-1", 0, admin);

    assertNotNull(comment.getDeletedTimestamp());
    ArgumentCaptor<CommentEvent> eventCaptor = ArgumentCaptor.forClass(CommentEvent.class);
    verify(commentPublisher, times(2)).publishCommentEvent(eventCaptor.capture());
  }

  private Comment buildComment(User owner) {
    return Comment.builder()
      .id("comment-1")
      .user(owner)
      .exercise(exercise)
      .content("content")
      .countReport(0)
      .build();
  }
}
