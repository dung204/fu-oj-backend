package com.example.modules.exams.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamCreateDTO;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.dtos.ExamUpdateDTO;
import com.example.modules.exams.dtos.ExamsSearchDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.entities.ExamSubmission;
import com.example.modules.exams.entities.GroupExam;
import com.example.modules.exams.enums.ExamStatus;
import com.example.modules.exams.exceptions.ExamNotFoundException;
import com.example.modules.exams.exceptions.ExamNotModifiableException;
import com.example.modules.exams.exceptions.InvalidTimeRangeException;
import com.example.modules.exams.exceptions.StartTimeTooSoonException;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.repositories.ExamSubmissionRepository;
import com.example.modules.exams.repositories.GroupExamRepository;
import com.example.modules.exams.utils.ExamMapper;
import com.example.modules.exams.utils.ExamsSpecification;
import com.example.modules.exams.utils.GroupExamSpecification;
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
  private final ExamSubmissionRepository examSubmissionRepository;
  private final GroupExamRepository groupExamRepository;
  private final GroupsRepository groupsRepository;
  private final ExercisesRepository exercisesRepository;
  private final ExamMapper examMapper;

  @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
  @Transactional
  public void updateExamStatus() {
    Instant now = Instant.now();
    log.info("=== Starting scheduled check for GroupExam status ===");

    // 1. Đóng các kỳ thi đang diễn ra (ONGOING -> COMPLETED)
    int completedCount = groupExamRepository
      .saveAll(
        groupExamRepository
          .findAll(
            GroupExamSpecification.builder()
              .withStatus(ExamStatus.ONGOING.getValue())
              .withExamEndTimeSmallerThanOrEqualTo(now)
              .build()
          )
          .stream()
          .map(groupExam -> {
            groupExam.setStatus(ExamStatus.COMPLETED);
            return groupExam;
          })
          .collect(Collectors.toList())
      )
      .size();

    if (completedCount > 0) {
      log.info("Marked {} group exams as COMPLETED.", completedCount);
    }

    // 2. Đánh hết hạn cho các kỳ thi bị quá hạn (UPCOMING -> OUTDATED)
    int outdatedCount = groupExamRepository
      .saveAll(
        groupExamRepository
          .findAll(
            GroupExamSpecification.builder()
              .withStatus(ExamStatus.UPCOMING.getValue())
              .withExamEndTimeSmallerThanOrEqualTo(now)
              .build()
          )
          .stream()
          .map(groupExam -> {
            groupExam.setStatus(ExamStatus.OUTDATED);
            return groupExam;
          })
          .collect(Collectors.toList())
      )
      .size();

    if (outdatedCount > 0) {
      log.info("Marked {} group exams as OUTDATED.", outdatedCount);
    }

    // 3. Bắt đầu các kỳ thi sắp tới (UPCOMING -> ONGOING)
    int ongoingCount = groupExamRepository
      .saveAll(
        groupExamRepository
          .findAll(
            GroupExamSpecification.builder()
              .withStatus(ExamStatus.UPCOMING.getValue())
              .withExamStartTimeSmallerThanOrEqualTo(now)
              .withExamEndTimeGreaterThan(now)
              .build()
          )
          .stream()
          .map(groupExam -> {
            groupExam.setStatus(ExamStatus.ONGOING);
            return groupExam;
          })
          .collect(Collectors.toList())
      )
      .size();

    if (ongoingCount > 0) {
      log.info("Marked {} group exams as ONGOING.", ongoingCount);
    }
    log.info("=== Scheduled check for GroupExam status completed ===");
  }

  /**
   * Tạo 1 exam cho nhiều group cùng lúc
   * Tạo 1 exam chung và các GroupExam để liên kết exam với các group
   */
  @Transactional
  public ExamResponseDTO createExamForMultipleGroups(ExamCreateDTO dto) {
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

    // Tạo 1 exam chung cho tất cả các group
    Exam exam = Exam.builder()
      .code(generateUniqueExamCode())
      .title(dto.getTitle())
      .description(dto.getDescription())
      .startTime(dto.getStartTime())
      .endTime(dto.getEndTime())
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
          .build();
        examExercises.add(examExercise);
      }
      examExerciseRepository.saveAll(examExercises);
      exam.setExamExercises(examExercises);
    }

    // Tạo GroupExam cho mỗi group
    List<GroupExam> groupExams = new ArrayList<>();
    for (Group group : groups) {
      GroupExam groupExam = GroupExam.builder()
        .exam(exam)
        .group(group)
        .status(ExamStatus.fromValue(dto.getStatus()))
        .build();
      groupExams.add(groupExam);
    }
    groupExamRepository.saveAll(groupExams);
    exam.setGroupExams(groupExams);

    log.info("Created exam {} for {} groups", exam.getCode(), groups.size());
    return examMapper.toExamResponseDTO(exam);
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
        exam = examRepository.findOne(ExamsSpecification.builder().withId(id).build());
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
            .withOwnerId(examsSearchDTO.getOwnerId())
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
    log.info("Updating exam with id {}", id);
    log.info("Updating exam with list groups {}", examUpdateDTO.getGroupIds().size());
    log.info("Updating exam with list exercise {}", examUpdateDTO.getExerciseIds().size());
    Exam exam = getExamById(id, currentUser);

    Instant now = Instant.now();

    // 1. Check if exam has already started or passed start time
    if (exam.getStartTime() != null && !now.isBefore(exam.getStartTime())) {
      throw new ExamNotModifiableException(
        "Cannot update exam that has already started or passed its start time."
      );
    }

    // 2. Check if there are any submissions for this exam
    List<ExamSubmission> submissions = examSubmissionRepository.findByExamId(exam.getId());
    if (submissions != null && !submissions.isEmpty()) {
      throw new ExamNotModifiableException("Cannot update exam that already has submissions.");
    }

    // 3. Check if any GroupExam has status that prevents modification
    List<GroupExam> groupExams = groupExamRepository.findByExamId(exam.getId());
    boolean hasNonModifiableStatus = groupExams
      .stream()
      .anyMatch(ge ->
        List.of(ExamStatus.ONGOING, ExamStatus.COMPLETED, ExamStatus.CANCELED).contains(
          ge.getStatus()
        )
      );

    if (hasNonModifiableStatus) {
      throw new ExamNotModifiableException();
    }

    ObjectUtils.assign(exam, examUpdateDTO);

    if (exam.getStartTime().isAfter(exam.getEndTime())) {
      throw new InvalidTimeRangeException();
    }

    Instant safeStartTime = now.plus(BUFFER_MINUTES, ChronoUnit.MINUTES);

    // Check if any GroupExam is UPCOMING and start time is too soon
    boolean hasUpcomingStatus = groupExams
      .stream()
      .anyMatch(ge -> ge.getStatus() == ExamStatus.UPCOMING);

    if (hasUpcomingStatus && exam.getStartTime().isBefore(safeStartTime)) {
      throw new StartTimeTooSoonException(
        "Start time must be after the current time at least " + BUFFER_MINUTES + " minutes."
      );
    }

    // 4. Update GroupExams if groupIds is provided
    if (examUpdateDTO.getGroupIds() != null && !examUpdateDTO.getGroupIds().isEmpty()) {
      log.info("Updating groups for exam {}", exam.getId());

      // Validate và lấy các group mới
      List<Group> newGroups = groupsRepository.findAllById(examUpdateDTO.getGroupIds());

      // Clear collection cũ (sẽ trigger orphanRemoval để xóa các GroupExam cũ)
      if (exam.getGroupExams() != null) {
        exam.getGroupExams().clear();
      } else {
        exam.setGroupExams(new ArrayList<>());
      }

      // Tạo và add GroupExam mới vào collection
      for (Group group : newGroups) {
        GroupExam groupExam = GroupExam.builder()
          .exam(exam)
          .group(group)
          .status(ExamStatus.DRAFT) // Mặc định là DRAFT khi tạo mới
          .build();
        exam.getGroupExams().add(groupExam);
      }

      log.info("Updated {} groups for exam {}", newGroups.size(), exam.getId());
    }

    // 5. Update ExamExercises if exerciseIds is provided
    if (examUpdateDTO.getExerciseIds() != null && !examUpdateDTO.getExerciseIds().isEmpty()) {
      log.info("Updating exercises for exam {}", exam.getId());

      // Validate và lấy các exercise mới
      List<Exercise> newExercises = exercisesRepository.findAllById(examUpdateDTO.getExerciseIds());

      // Clear collection cũ (sẽ trigger orphanRemoval để xóa các ExamExercise cũ)
      if (exam.getExamExercises() != null) {
        exam.getExamExercises().clear();
      } else {
        exam.setExamExercises(new ArrayList<>());
      }

      // Tạo và add ExamExercise mới vào collection với thứ tự
      for (int i = 0; i < newExercises.size(); i++) {
        ExamExercise examExercise = ExamExercise.builder()
          .exam(exam)
          .exercise(newExercises.get(i))
          .order(i + 1)
          .build();
        exam.getExamExercises().add(examExercise);
      }

      log.info("Updated {} exercises for exam {}", newExercises.size(), exam.getId());
    }

    return examMapper.toExamResponseDTO(examRepository.save(exam));
  }

  @Transactional
  public ExamResponseDTO publishExam(String id, User currentUser) {
    Exam exam = getExamById(id, currentUser);

    // Check if all GroupExams are in DRAFT status
    List<GroupExam> groupExams = groupExamRepository.findByExamId(exam.getId());
    boolean hasNonDraftStatus = groupExams
      .stream()
      .anyMatch(ge -> ge.getStatus() != ExamStatus.DRAFT);

    if (hasNonDraftStatus) {
      throw new ExamNotModifiableException(
        "Exam can not be published when any group exam status is not 'DRAFT'."
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

    // Update all GroupExams status to UPCOMING
    groupExams.forEach(ge -> ge.setStatus(ExamStatus.UPCOMING));
    groupExamRepository.saveAll(groupExams);

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
