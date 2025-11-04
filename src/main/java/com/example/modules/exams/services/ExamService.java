package com.example.modules.exams.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamCreateDTO;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.dtos.ExamUpdateDTO;
import com.example.modules.exams.dtos.ExamsSearchDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.enums.ExamStatus;
import com.example.modules.exams.exceptions.ExamNotFoundException;
import com.example.modules.exams.exceptions.ExamNotModifiableException;
import com.example.modules.exams.exceptions.InvalidTimeRangeException;
import com.example.modules.exams.exceptions.StartTimeTooSoonException;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.utils.ExamMapper;
import com.example.modules.exams.utils.ExamsSpecification;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.users.entities.User;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExamService {

  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom random = new SecureRandom();
  private final long BUFFER_MINUTES = 5;

  private final ExamRepository examRepository;
  private final ExamExerciseRepository examExerciseRepository;
  private final GroupsRepository groupsRepository;
  private final ExercisesRepository exercisesRepository;
  private final ExamMapper examMapper;

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  @Transactional
  public void updateExamStatus() {
    Instant now = Instant.now();
    log.info("=== Starting scheduled check for Exam status ===");

    // 1. Đóng các kỳ thi đang diễn ra (ONGOING -> COMPLETED)
    int completedCount = examRepository
      .saveAll(
        examRepository
          .findAll(
            ExamsSpecification.builder()
              .withStatus(ExamStatus.ONGOING.getValue())
              .withEndTimeSmallerThanOrEqualTo(now)
              .build()
          )
          .stream()
          .map(exam -> {
            exam.setStatus(ExamStatus.COMPLETED);
            return exam;
          })
          .collect(Collectors.toList())
      )
      .size();

    if (completedCount > 0) {
      log.info("Marked {} exams as COMPLETED.", completedCount);
    }

    // 2. Đánh hết hạn cho các kỳ thi bị quá hạn (UPCOMING -> OUTDATED)
    int outdatedCount = examRepository
      .saveAll(
        examRepository
          .findAll(
            ExamsSpecification.builder()
              .withStatus(ExamStatus.UPCOMING.getValue())
              .withEndTimeSmallerThanOrEqualTo(now)
              .build()
          )
          .stream()
          .map(exam -> {
            exam.setStatus(ExamStatus.OUTDATED);
            return exam;
          })
          .collect(Collectors.toList())
      )
      .size();

    if (outdatedCount > 0) {
      log.info("Marked {} exams as OUTDATED.", outdatedCount);
    }

    int ongoingCount = examRepository
      .saveAll(
        examRepository
          .findAll(
            ExamsSpecification.builder()
              .withStatus(ExamStatus.UPCOMING.getValue())
              .withEndTimeGreaterThan(now)
              .build()
          )
          .stream()
          .map(exam -> {
            exam.setStatus(ExamStatus.ONGOING);
            return exam;
          })
          .collect(Collectors.toList())
      )
      .size();

    if (ongoingCount > 0) {
      log.info("Marked {} exams as OUTDATED.", outdatedCount);
    }
    log.info("=== Scheduled check for Exam status completed ===");
  }

  /**
   * Tạo exam cho nhiều group cùng lúc
   * Với mỗi group, sẽ tạo 1 exam riêng với title = "{title} {groupName}"
   */
  @Transactional
  public List<ExamResponseDTO> createExamsForMultipleGroups(ExamCreateDTO dto, User currentUser) {
    List<ExamResponseDTO> createdExams = new ArrayList<>();

    if (dto.getStartTime().isAfter(dto.getEndTime())) {
      throw new InvalidTimeRangeException();
    }

    Instant now = Instant.now();
    Instant safeStartTime = now.plus(BUFFER_MINUTES, ChronoUnit.MINUTES);

    if (
      dto.getStatus().equals(ExamStatus.UPCOMING.getValue()) &&
      dto.getStartTime().isBefore(safeStartTime)
    ) {
      throw new StartTimeTooSoonException(
        "Start time must be after the current time at least " + BUFFER_MINUTES + " minutes."
      );
    }

    // Validate và lấy các group
    List<Group> groups = groupsRepository.findAllById(dto.getGroupIds());

    // Validate và lấy các exercise
    List<Exercise> exercises = new ArrayList<>();
    if (dto.getExerciseIds() != null && !dto.getExerciseIds().isEmpty()) {
      exercises = exercisesRepository.findAllById(dto.getExerciseIds());
    }

    // Tạo exam cho mỗi group
    for (Group group : groups) {
      // Tạo title cho exam: "{title gốc} {tên group}"
      String examTitle = dto.getTitle() + " " + group.getName();

      // Tạo exam
      Exam exam = Exam.builder()
        .code(generateUniqueExamCode())
        .title(examTitle)
        .description(dto.getDescription())
        .status(ExamStatus.fromValue(dto.getStatus()))
        .startTime(dto.getStartTime())
        .endTime(dto.getEndTime())
        .group(group)
        .createdBy(currentUser.getId())
        .build();

      exam = examRepository.save(exam);

      // Tạo các ExamExercise
      if (!exercises.isEmpty()) {
        List<ExamExercise> examExercises = new ArrayList<>();
        for (int i = 0; i < exercises.size(); i++) {
          ExamExercise examExercise = ExamExercise.builder()
            .exam(exam)
            .exercise(exercises.get(i))
            .order(i + 1)
            .createdBy(currentUser.getId())
            .build();

          // set createdBy
          examExercises.add(examExercise);
        }
        examExerciseRepository.saveAll(examExercises);
        exam.setExamExercises(examExercises);
      }

      createdExams.add(examMapper.toExamResponseDTO(exam));
    }

    log.info("Created {} exams for {} groups", createdExams.size(), groups.size());
    return createdExams;
  }

  /**
   * Lấy exam theo ID
   */
  public Exam getExamById(String id, User currentUser) {
    Optional<Exam> exam = null;

    switch (currentUser.getAccount().getRole()) {
      case Role.STUDENT:
        exam = examRepository.findOne(
          ExamsSpecification.builder()
            .belongsToGroups(currentUser.getJoinedGroups().stream().map(Group::getId).toList())
            .withId(id)
            .build()
        );
        break;
      case Role.INSTRUCTOR:
        exam = examRepository.findOne(
          ExamsSpecification.builder().createdBy(currentUser.getId()).withId(id).build()
        );
        break;
      case Role.ADMIN:
        exam = examRepository.findOne(ExamsSpecification.builder().withId(id).build());
        break;
    }

    return exam.orElseThrow(() ->
      new ExamNotFoundException("Exam with id %s not found".formatted(id))
    );
  }

  /**
   * Lấy tất cả exam
   */
  public Page<ExamResponseDTO> getAllExams(ExamsSearchDTO examsSearchDTO, User currentUser) {
    Page<Exam> examsPage = null;

    switch (currentUser.getAccount().getRole()) {
      case Role.STUDENT:
        examsPage = examRepository.findAll(
          ExamsSpecification.builder()
            .belongsToGroups(currentUser.getJoinedGroups().stream().map(Group::getId).toList())
            .withGroupId(examsSearchDTO.getGroupId())
            .containsCodeOrContainsTitle(examsSearchDTO.getQuery())
            .isOneOfStatuses(examsSearchDTO.getStatus())
            .build(),
          examsSearchDTO.toPageRequest()
        );
        break;
      case Role.INSTRUCTOR:
        examsPage = examRepository.findAll(
          ExamsSpecification.builder()
            .withGroupId(examsSearchDTO.getGroupId())
            .containsCodeOrContainsTitle(examsSearchDTO.getQuery())
            .isOneOfStatuses(examsSearchDTO.getStatus())
            .createdBy(currentUser.getId())
            .build(),
          examsSearchDTO.toPageRequest()
        );
        break;
      case Role.ADMIN:
        examsPage = examRepository.findAll(
          ExamsSpecification.builder()
            .withGroupId(examsSearchDTO.getGroupId())
            .containsCodeOrContainsTitle(examsSearchDTO.getQuery())
            .isOneOfStatuses(examsSearchDTO.getStatus())
            .build(),
          examsSearchDTO.toPageRequest()
        );
        break;
    }

    return examsPage.map(examMapper::toExamResponseDTO);
  }

  @Transactional
  public ExamResponseDTO updateExam(String id, ExamUpdateDTO examUpdateDTO, User currentUser) {
    Exam exam = getExamById(id, currentUser);

    if (
      List.of(ExamStatus.ONGOING, ExamStatus.COMPLETED, ExamStatus.CANCELED).contains(
        exam.getStatus()
      )
    ) {
      throw new ExamNotModifiableException();
    }

    ObjectUtils.assign(exam, examUpdateDTO);

    if (exam.getStartTime().isAfter(exam.getEndTime())) {
      throw new InvalidTimeRangeException();
    }

    Instant now = Instant.now();
    Instant safeStartTime = now.plus(BUFFER_MINUTES, ChronoUnit.MINUTES);

    if (exam.getStatus() == ExamStatus.UPCOMING && exam.getStartTime().isBefore(safeStartTime)) {
      throw new StartTimeTooSoonException(
        "Start time must be after the current time at least " + BUFFER_MINUTES + " minutes."
      );
    }

    return examMapper.toExamResponseDTO(examRepository.save(exam));
  }

  @Transactional
  public ExamResponseDTO publishExam(String id, User currentUser) {
    Exam exam = getExamById(id, currentUser);

    if (exam.getStatus() != ExamStatus.DRAFT) {
      throw new ExamNotModifiableException(
        "Exam can not be published when the status is not 'DRAFT'."
      );
    }

    if (exam.getStartTime().isAfter(exam.getEndTime())) {
      throw new InvalidTimeRangeException();
    }

    Instant now = Instant.now();
    Instant safeStartTime = now.plus(BUFFER_MINUTES, ChronoUnit.MINUTES);

    if (exam.getStartTime().isBefore(safeStartTime)) {
      throw new StartTimeTooSoonException(
        "Start time must be after the current time at least " + BUFFER_MINUTES + " minutes."
      );
    }

    exam.setStatus(ExamStatus.UPCOMING);
    return examMapper.toExamResponseDTO(examRepository.save(exam));
  }

  /**
   * Xóa exam (soft delete)
   */
  @Transactional
  public ExamResponseDTO deleteExam(String id, User currentUser) {
    Exam exam = getExamById(id, currentUser);
    exam.softDelete();
    examRepository.save(exam);
    return examMapper.toExamResponseDTO(exam);
  }

  /**
   * Generate unique exam code
   */
  @Transactional
  public String generateUniqueExamCode() {
    String code;
    do {
      code = "EXAM-" + generateCode(6);
    } while (examRepository.existsByCode(code));
    return code;
  }

  private String generateCode(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
    }
    return sb.toString();
  }
}
