package com.example.modules.exams.services;

import com.example.modules.exams.dtos.ExamResultRequestDto;
import com.example.modules.exams.dtos.ExamResultResponseDto;
import com.example.modules.exams.dtos.ExamSubmissionCreateDto;
import com.example.modules.exams.dtos.ExamSubmissionResponseDto;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.exceptions.*;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
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
  private final ExercisesRepository exercisesRepository;
  private final SubmissionsService submissionsService;
  private final SubmissionsRepository submissionsRepository;
  private final UsersRepository usersRepository;

  /**
   * Nộp bài cho exam (từng bài 1)
   * Preconditions:
   * - Phải trong khoảng startTime và endTime
   * - Sinh viên phải có trong group của exam
   * - Exercise phải thuộc exam
   */
  @Transactional
  public ExamSubmissionResponseDto createExamSubmission(
    ExamSubmissionCreateDto dto,
    User currentUser
  ) {
    // 1. Validate exam exists
    Exam exam = examRepository
      .findById(dto.getExamId())
      .orElseThrow(() ->
        new ExamNotFoundException("Exam with id " + dto.getExamId() + " not found")
      );

    // 2. Check time constraints
    Instant now = Instant.now();
    if (now.isBefore(exam.getStartTime())) {
      throw new ExamNotStartedException(
        "Exam starts at " + exam.getStartTime() + ", current time: " + now
      );
    }
    if (now.isAfter(exam.getEndTime())) {
      throw new ExamEndedException("Exam ended at " + exam.getEndTime() + ", current time: " + now);
    }

    // 3. Check if student is in the exam's group
    if (exam.getGroup() != null) {
      boolean isInGroup = exam
        .getGroup()
        .getStudents()
        .stream()
        .anyMatch(student -> student.getId().equals(currentUser.getId()));

      if (!isInGroup) {
        throw new StudentNotInGroupException(
          "Student " + currentUser.getId() + " is not in group " + exam.getGroup().getId()
        );
      }
    }

    // 4. Validate exercise exists and belongs to exam
    Exercise exercise = exercisesRepository
      .findById(dto.getExerciseId())
      .orElseThrow(() -> new ExerciseNotFoundException("Exercise not found"));

    boolean exerciseInExam = exam
      .getExamExercises()
      .stream()
      .anyMatch(ee -> ee.getExercise().getId().equals(dto.getExerciseId()));

    if (!exerciseInExam) {
      throw new ExerciseNotInExamException(
        "Exercise " + dto.getExerciseId() + " is not part of exam " + dto.getExamId()
      );
    }

    // 5. Create submission through existing SubmissionsService
    SubmissionRequest submissionRequest = new SubmissionRequest();
    submissionRequest.setExerciseId(dto.getExerciseId());
    submissionRequest.setSourceCode(dto.getSourceCode());
    submissionRequest.setLanguageCode(dto.getLanguageCode());

    SubmissionResponseDTO submissionResponse = submissionsService.createSubmissionBase64(
      submissionRequest,
      currentUser
    );

    // 6. Create ExamSubmission record
    ExamSubmission examSubmission = ExamSubmission.builder()
      .exam(exam)
      .user(currentUser)
      .exercise(exercise)
      .submissionId(submissionResponse.getId())
      .score(null) // Will be updated after Judge0 callback
      .build();

    examSubmission.setCreatedBy(currentUser.getId());
    examSubmission = examSubmissionRepository.save(examSubmission);

    log.info(
      "Created exam submission for user {} on exam {} exercise {}",
      currentUser.getId(),
      dto.getExamId(),
      dto.getExerciseId()
    );

    return ExamSubmissionResponseDto.builder()
      .id(examSubmission.getId())
      .examId(exam.getId())
      .userId(currentUser.getId())
      .exerciseId(exercise.getId())
      .submissionId(submissionResponse.getId())
      .score(examSubmission.getScore())
      .build();
  }

  /**
   * Xem lại kết quả exam của student
   */
  @Transactional(readOnly = true)
  public ExamResultResponseDto getExamResult(ExamResultRequestDto dto) {
    // 1. Validate exam exists
    Exam exam = examRepository
      .findById(dto.getExamId())
      .orElseThrow(() ->
        new ExamNotFoundException("Exam with id " + dto.getExamId() + " not found")
      );

    // 2. Validate user exists
    User user = usersRepository
      .findById(dto.getUserId())
      .orElseThrow(() ->
        new UserNotFoundException("User with id " + dto.getUserId() + " not found")
      );

    // 3. Get all exam submissions for this user
    List<ExamSubmission> examSubmissions = examSubmissionRepository.findByExamIdAndUserId(
      dto.getExamId(),
      dto.getUserId()
    );

    // 4. Build submission details
    List<ExamResultResponseDto.ExamSubmissionDetail> submissionDetails = new ArrayList<>();
    double totalScore = 0.0;
    int completedExercises = 0;

    for (ExamSubmission examSubmission : examSubmissions) {
      // Get the actual submission
      Submission submission = submissionsRepository
        .findById(examSubmission.getSubmissionId())
        .orElse(null);

      if (submission != null) {
        ExamResultResponseDto.ExamSubmissionDetail detail =
          ExamResultResponseDto.ExamSubmissionDetail.builder()
            .exerciseId(examSubmission.getExercise().getId())
            .exerciseTitle(examSubmission.getExercise().getTitle())
            .exerciseCode(examSubmission.getExercise().getCode())
            .submissionId(submission.getId())
            .score(submission.getScore())
            .isAccepted(submission.getIsAccepted())
            .passedTestCases(submission.getPassedTestCases())
            .totalTestCases(submission.getTotalTestCases())
            .submittedAt(examSubmission.getCreatedTimestamp())
            .build();

        submissionDetails.add(detail);

        if (submission.getScore() != null) {
          totalScore += submission.getPassedTestCases();
        }

        if (submission.getIsAccepted() != null && submission.getIsAccepted()) {
          completedExercises++;
        }
      }
    }

    // 5. Build response
    return ExamResultResponseDto.builder()
      .examId(exam.getId())
      .examCode(exam.getCode())
      .examTitle(exam.getTitle())
      .startTime(exam.getStartTime())
      .endTime(exam.getEndTime())
      .userId(user.getId())
      .userName(user.getFirstName() + " " + user.getLastName())
      .submissions(submissionDetails)
      .totalScore(totalScore)
      .totalExercises(exam.getExamExercises() != null ? exam.getExamExercises().size() : 0)
      .completedExercises(completedExercises)
      .build();
  }
}
