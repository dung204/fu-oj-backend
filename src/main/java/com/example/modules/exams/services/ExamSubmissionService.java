package com.example.modules.exams.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamResultRequestDTO;
import com.example.modules.exams.dtos.ExamResultResponseDTO;
import com.example.modules.exams.dtos.ExamSubmissionCreateDTO;
import com.example.modules.exams.dtos.ExamSubmissionResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exams.exceptions.*;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.repositories.GroupExamRepository;
import com.example.modules.exams.utils.ExamSubmissionSpecification;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.submissions.dtos.SubmissionRequest;
import com.example.modules.submissions.dtos.SubmissionResponseDTO;
import com.example.modules.submissions.entities.Submission;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.submissions.services.SubmissionsService;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExamSubmissionService {

  private final ExamRepository examRepository;
  private final ExamSubmissionRepository examSubmissionRepository;
  private final GroupExamRepository groupExamRepository;
  private final ExercisesRepository exercisesRepository;
  private final SubmissionsService submissionsService;
  private final SubmissionsRepository submissionsRepository;
  private final UsersRepository usersRepository;

  /**
   * Nộp bài cho group exam (từng bài 1)
   * Preconditions:
   * - Phải trong khoảng startTime và endTime
   * - Sinh viên phải có trong group của groupExam
   * - Exercise phải thuộc exam
   */
  //  @Transactional
  public ExamSubmissionResponseDTO createExamSubmission(
    ExamSubmissionCreateDTO dto,
    User currentUser
  ) {
    // 1. Validate groupExam exists
    GroupExam groupExam = groupExamRepository
      .findById(dto.getGroupExamId())
      .orElseThrow(() ->
        new ExamNotFoundException("Không tìm thấy GroupExam với id " + dto.getGroupExamId())
      );

    Exam exam = groupExam.getExam();
    if (exam == null) {
      throw new ExamNotFoundException(
        "Không tìm thấy kỳ thi cho GroupExam " + dto.getGroupExamId()
      );
    }

    // 2. Check time constraints
    Instant now = Instant.now();
    if (now.isBefore(exam.getStartTime())) {
      throw new ExamNotStartedException(
        "Kỳ thi bắt đầu lúc " + exam.getStartTime() + ", thời gian hiện tại: " + now
      );
    }
    if (now.isAfter(exam.getEndTime())) {
      throw new ExamEndedException(
        "Kỳ thi kết thúc lúc " + exam.getEndTime() + ", thời gian hiện tại: " + now
      );
    }

    // 3. Check if student is in the group
    boolean isInGroup = groupExam
      .getGroup()
      .getStudents()
      .stream()
      .anyMatch(student -> student.getId().equals(currentUser.getId()));

    if (!isInGroup) {
      throw new StudentNotInGroupException(
        "Sinh viên " +
          currentUser.getId() +
          " không thuộc nhóm của groupExam " +
          dto.getGroupExamId()
      );
    }

    // 4. Validate exercise exists and belongs to exam
    Exercise exercise = exercisesRepository
      .findById(dto.getExerciseId())
      .orElseThrow(() -> new ExerciseNotFoundException("Không tìm thấy bài tập"));

    boolean exerciseInExam = exam
      .getExamExercises()
      .stream()
      .anyMatch(ee -> ee.getExercise().getId().equals(dto.getExerciseId()));

    if (!exerciseInExam) {
      throw new ExerciseNotInExamException(
        "Bài tập " + dto.getExerciseId() + " không thuộc kỳ thi " + exam.getId()
      );
    }

    // 4.1 check if user has already submitted for this exercise in this groupExam
    List<ExamSubmission> existingSubmission =
      examSubmissionRepository.findByGroupExamIdAndUserIdAndExerciseId(
        dto.getGroupExamId(),
        currentUser.getId(),
        dto.getExerciseId()
      );

    if (existingSubmission != null && existingSubmission.size() > 0) {
      log.info(
        "User {} has already submitted for exercise {} in groupExam {}",
        currentUser.getId(),
        dto.getExerciseId(),
        dto.getGroupExamId()
      );
      throw new DuplicateExamSubmissionException("Người dùng đã nộp bài cho bài tập này");
    }

    // 5. Create submission through existing SubmissionsService
    SubmissionRequest submissionRequest = new SubmissionRequest();
    submissionRequest.setExerciseId(dto.getExerciseId());
    submissionRequest.setSourceCode(dto.getSourceCode());
    submissionRequest.setLanguageCode(dto.getLanguageCode());
    submissionRequest.setExamination(true);

    SubmissionResponseDTO submissionResponse = submissionsService.createSubmissionBase64(
      submissionRequest,
      currentUser
    );

    // 6. Create ExamSubmission record
    ExamSubmission examSubmission = ExamSubmission.builder()
      .groupExam(groupExam)
      .user(currentUser)
      .exercise(exercise)
      .submissionId(submissionResponse.getId())
      .score(null) // Will be updated after Judge0 callback
      .build();

    examSubmission = examSubmissionRepository.save(examSubmission);

    log.info(
      "Created exam submission for user {} on groupExam {} exercise {}",
      currentUser.getId(),
      dto.getGroupExamId(),
      dto.getExerciseId()
    );

    return ExamSubmissionResponseDTO.builder()
      .id(examSubmission.getId())
      .groupExamId(groupExam.getId())
      .userId(currentUser.getId())
      .exerciseId(exercise.getId())
      .submissionId(submissionResponse.getId())
      .score(examSubmission.getScore())
      .build();
  }

  /**
   * lấy tất cả exercise submission của user trong exam hoặc groupExam
   */
  public ExamResultResponseDTO getExamResult(ExamResultRequestDTO dto, User currentUser) {
    // 1. Validate: phải có examId hoặc groupExamId
    if (dto.getExamId() == null && dto.getGroupExamId() == null) {
      throw new IllegalArgumentException("Phải cung cấp examId hoặc groupExamId");
    }

    // 2. Validate user exists và check quyền truy cập
    // STUDENT chỉ được xem kết quả của chính mình
    final String effectiveUserId;
    if (currentUser.getAccount().getRole() == Role.STUDENT) {
      effectiveUserId = currentUser.getId();
      if (!currentUser.getId().equals(dto.getUserId())) {
        throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.FORBIDDEN,
          "Sinh viên chỉ có thể xem kết quả của chính mình"
        );
      }
    } else {
      effectiveUserId = dto.getUserId();
    }

    User user = usersRepository
      .findById(effectiveUserId)
      .orElseThrow(() ->
        new UserNotFoundException("Không tìm thấy người dùng với id " + effectiveUserId)
      );

    // 3. Query submissions based on examId or groupExamId
    List<ExamSubmission> examSubmissions;
    Exam exam;

    if (dto.getGroupExamId() != null) {
      // Query by groupExamId
      GroupExam groupExam = groupExamRepository
        .findById(dto.getGroupExamId())
        .orElseThrow(() ->
          new ExamNotFoundException("Không tìm thấy GroupExam với id " + dto.getGroupExamId())
        );
      exam = groupExam.getExam();

      var spec = ExamSubmissionSpecification.builder()
        .withGroupExamId(dto.getGroupExamId())
        .withUserId(effectiveUserId)
        .notDeleted()
        .build();
      examSubmissions = examSubmissionRepository.findAll(spec);
    } else {
      // Query by examId (across all groupExams)
      exam = examRepository
        .findById(dto.getExamId())
        .orElseThrow(() ->
          new ExamNotFoundException("Không tìm thấy kỳ thi với id " + dto.getExamId())
        );

      examSubmissions = examSubmissionRepository.findByGroupExam_Exam_IdAndUserId(
        dto.getExamId(),
        effectiveUserId
      );
    }

    // 4. Build submission details
    List<ExamResultResponseDTO.ExamSubmissionDetail> submissionDetails = new ArrayList<>();
    int completedExercises = 0;

    for (ExamSubmission examSubmission : examSubmissions) {
      ExamResultResponseDTO.ExamSubmissionDetail detail;

      // Check if this is auto-submitted (submissionId = null)
      if (examSubmission.getSubmissionId() == null) {
        // Auto-submitted exercise: use data from ExamSubmission directly
        detail = ExamResultResponseDTO.ExamSubmissionDetail.builder()
          .exerciseId(examSubmission.getExercise().getId())
          .exerciseTitle(examSubmission.getExercise().getTitle())
          .exerciseCode(examSubmission.getExercise().getCode())
          .submissionId(null) // No actual submission
          .score(examSubmission.getScore()) // Always 0.0 for auto-submit
          .isAccepted(false) // Auto-submit never accepted
          .passedTestCases(0)
          .totalTestCases(0)
          .submittedAt(examSubmission.getCreatedTimestamp())
          .build();

        submissionDetails.add(detail);
        // Score already 0, no need to add to totalScore or completedExercises
      } else {
        // Normal submission: get details from Submission entity
        Submission submission = submissionsRepository
          .findById(examSubmission.getSubmissionId())
          .orElse(null);

        if (submission != null) {
          detail = ExamResultResponseDTO.ExamSubmissionDetail.builder()
            .exerciseId(examSubmission.getExercise().getId())
            .exerciseTitle(examSubmission.getExercise().getTitle())
            .exerciseCode(examSubmission.getExercise().getCode())
            .submissionId(submission.getId())
            .score(submission.getScore() != null ? submission.getScore() : 0.0)
            .isAccepted(submission.getIsAccepted())
            .passedTestCases(submission.getPassedTestCases())
            .totalTestCases(submission.getTotalTestCases())
            .submittedAt(examSubmission.getCreatedTimestamp())
            .build();

          submissionDetails.add(detail);

          if (submission.getIsAccepted() != null && submission.getIsAccepted()) {
            completedExercises++;
          }
        }
      }
    }

    Integer totalExercises = exam.getExamExercises() != null ? exam.getExamExercises().size() : 0;
    Double totalScore = totalExercises > 0
      ? (completedExercises / (double) totalExercises) * 100.0
      : 0.0;

    // 5. Build response
    return ExamResultResponseDTO.builder()
      .examId(exam.getId())
      .examCode(exam.getCode())
      .examTitle(exam.getTitle())
      .startTime(exam.getStartTime())
      .endTime(exam.getEndTime())
      .userId(user.getId())
      .userName(user.getFirstName() + " " + user.getLastName())
      .submissions(submissionDetails)
      .totalScore(totalScore)
      .totalExercises(totalExercises)
      .completedExercises(completedExercises)
      .timeLimit(exam.getTimeLimit())
      .build();
  }
}
