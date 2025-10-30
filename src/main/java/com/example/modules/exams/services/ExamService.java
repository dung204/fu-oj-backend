package com.example.modules.exams.services;

import com.example.modules.exams.dtos.ExamCreateDto;
import com.example.modules.exams.dtos.ExamResponseDTO;
import com.example.modules.exams.entities.Exam;
import com.example.modules.exams.entities.ExamExercise;
import com.example.modules.exams.enums.Status;
import com.example.modules.exams.exceptions.ExamNotFoundException;
import com.example.modules.exams.repositories.ExamExerciseRepository;
import com.example.modules.exams.repositories.ExamRepository;
import com.example.modules.exams.utils.ExamMapper;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.groups.entities.Group;
import com.example.modules.groups.exeptions.GroupNotFoundException;
import com.example.modules.groups.repositories.GroupsRepository;
import com.example.modules.users.entities.User;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExamService {

  private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final SecureRandom random = new SecureRandom();

  private final ExamRepository examRepository;
  private final ExamExerciseRepository examExerciseRepository;
  private final GroupsRepository groupsRepository;
  private final ExercisesRepository exercisesRepository;
  private final ExamMapper examMapper;

  /**
   * Tạo exam cho nhiều group cùng lúc
   * Với mỗi group, sẽ tạo 1 exam riêng với title = "{title} {groupName}"
   */
  @Transactional
  public List<ExamResponseDTO> createExamsForMultipleGroups(ExamCreateDto dto, User currentUser) {
    List<ExamResponseDTO> createdExams = new ArrayList<>();

    // Validate và lấy các group
    List<Group> groups = groupsRepository.findAllById(dto.getGroupId());
    if (groups.isEmpty()) {
      throw new GroupNotFoundException();
    }

    // Validate và lấy các exercise
    List<Exercise> exercises = new ArrayList<>();
    if (dto.getExerciseIds() != null && !dto.getExerciseIds().isEmpty()) {
      exercises = exercisesRepository.findAllById(dto.getExerciseIds());
    }

    // Parse status
    Status status = Status.UPCOMING;
    if (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) {
      try {
        status = Status.valueOf(dto.getStatus().toUpperCase());
      } catch (IllegalArgumentException e) {
        log.warn("Invalid status: {}, using default UPCOMING", dto.getStatus());
      }
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
        .status(status)
        .startTime(dto.getStartTime())
        .endTime(dto.getEndTime())
        .group(group)
        .build();

      // set createdBy
      exam.setCreatedBy(currentUser.getId());

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

          // set createdBy
          examExercise.setCreatedBy(currentUser.getId());
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
  public ExamResponseDTO getExamById(String id) {
    Exam exam = examRepository
      .findById(id)
      .orElseThrow(() -> new ExamNotFoundException("Exam with id " + id + " not found"));
    return examMapper.toExamResponseDTO(exam);
  }

  /**
   * Lấy tất cả exam
   */
  public List<ExamResponseDTO> getAllExams() {
    return examRepository
      .findAll()
      .stream()
      .map(examMapper::toExamResponseDTO)
      .collect(Collectors.toList());
  }

  /**
   * Xóa exam (soft delete)
   */
  @Transactional
  public ExamResponseDTO deleteExam(String id) {
    Exam exam = examRepository
      .findById(id)
      .orElseThrow(() -> new ExamNotFoundException("Exam with id " + id + " not found"));
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
