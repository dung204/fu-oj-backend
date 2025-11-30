package com.example.modules.dashboard.services;

import com.example.modules.dashboard.dtos.InstructorDashboardRequestDTO;
import com.example.modules.dashboard.dtos.InstructorDashboardStatsDTO;
import com.example.modules.dashboard.utils.DashboardInstructorSpecification;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class DashBoardInstructorService {

  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Lấy thống kê dashboard với filter sử dụng specification pattern
   */
  @Transactional(readOnly = true)
  public InstructorDashboardStatsDTO getDashboardStatsByGroup(
    String instructorId,
    InstructorDashboardRequestDTO request
  ) {
    // Build specification từ request
    DashboardInstructorSpecification spec = DashboardInstructorSpecification.builder()
      .instructorId(instructorId)
      .groupId(request.getGroupId())
      .build();

    if (spec.hasGroupFilter()) {
      log.info(
        "Lấy thống kê dashboard cho instructorId: {} và groupId: {}",
        spec.getInstructorId(),
        spec.getGroupId()
      );
    } else {
      log.info(
        "Lấy thống kê dashboard cho instructorId: {} (tất cả groups)",
        spec.getInstructorId()
      );
    }

    return getDashboardStatsBySpec(spec);
  }

  /**
   * Lấy thống kê dashboard theo specification
   */
  private InstructorDashboardStatsDTO getDashboardStatsBySpec(
    DashboardInstructorSpecification spec
  ) {
    // 1. Đếm tổng số groups
    Long totalGroups = countGroupsBySpec(spec);

    // 2. Đếm tổng số students
    Long totalStudents = countStudentsBySpec(spec);

    // 3. Đếm tổng số exams
    Long totalExams = countExamsBySpec(spec);

    // 4. Đếm tổng số exercises
    Long totalExercises = countExercisesBySpec(spec);

    // 5. Đếm số exams diễn ra trong ngày hôm nay
    Long examsComing = countExamsComingTodayBySpec(spec);

    return InstructorDashboardStatsDTO.builder()
      .totalGroups(totalGroups)
      .totalStudents(totalStudents)
      .totalExams(totalExams)
      .totalExercises(totalExercises)
      .examsComing(examsComing)
      .build();
  }

  // ==================== Helper methods (reused by spec methods) ====================

  private Long countGroupsByInstructor(String instructorId) {
    String query =
      "SELECT COUNT(g) FROM Group g " +
      "WHERE g.instructor.id = :instructorId AND g.deletedTimestamp IS NULL";
    return entityManager
      .createQuery(query, Long.class)
      .setParameter("instructorId", instructorId)
      .getSingleResult();
  }

  private Long countStudentsByInstructor(String instructorId) {
    String query =
      "SELECT COUNT(DISTINCT s.id) FROM Group g " +
      "JOIN g.students s " +
      "WHERE g.instructor.id = :instructorId AND g.deletedTimestamp IS NULL";
    return entityManager
      .createQuery(query, Long.class)
      .setParameter("instructorId", instructorId)
      .getSingleResult();
  }

  private Long countExamsByInstructor(String instructorId) {
    String query =
      "SELECT COUNT(DISTINCT e.id) FROM Group g " +
      "JOIN g.groupExams ge " +
      "JOIN ge.exam e " +
      "WHERE g.instructor.id = :instructorId " +
      "AND g.deletedTimestamp IS NULL " +
      "AND e.deletedTimestamp IS NULL";
    return entityManager
      .createQuery(query, Long.class)
      .setParameter("instructorId", instructorId)
      .getSingleResult();
  }

  private Long countExercisesByInstructor(String instructorId) {
    // Đếm số lượng exercises mà giáo viên đã tạo (dựa vào createdBy)
    String query =
      "SELECT COUNT(e.id) " +
      "FROM exercises e " +
      "WHERE e.created_by = ?1 " +
      "AND e.deleted_timestamp IS NULL";

    return (
      (Number) entityManager
        .createNativeQuery(query)
        .setParameter(1, instructorId)
        .getSingleResult()
    ).longValue();
  }

  private Long countExamsComingToday(String instructorId) {
    // Lấy thời gian bắt đầu và kết thúc của ngày hôm nay
    LocalDate today = LocalDate.now();
    ZonedDateTime startOfDay = today.atStartOfDay(ZoneId.systemDefault());
    ZonedDateTime endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault());

    Instant startInstant = startOfDay.toInstant();
    Instant endInstant = endOfDay.toInstant();

    String query =
      "SELECT COUNT(DISTINCT e.id) FROM Group g " +
      "JOIN g.groupExams ge " +
      "JOIN ge.exam e " +
      "WHERE g.instructor.id = :instructorId " +
      "AND g.deletedTimestamp IS NULL " +
      "AND e.deletedTimestamp IS NULL " +
      "AND e.startTime >= :startOfDay " +
      "AND e.startTime < :endOfDay";

    return entityManager
      .createQuery(query, Long.class)
      .setParameter("instructorId", instructorId)
      .setParameter("startOfDay", startInstant)
      .setParameter("endOfDay", endInstant)
      .getSingleResult();
  }

  // ==================== Methods với Specification Pattern ====================

  private Long countGroupsBySpec(DashboardInstructorSpecification spec) {
    if (spec.hasGroupFilter()) {
      String query =
        "SELECT COUNT(g) FROM Group g " +
        "WHERE g.instructor.id = :instructorId " +
        "AND g.id = :groupId " +
        "AND g.deletedTimestamp IS NULL";
      return entityManager
        .createQuery(query, Long.class)
        .setParameter("instructorId", spec.getInstructorId())
        .setParameter("groupId", spec.getGroupId())
        .getSingleResult();
    }
    return countGroupsByInstructor(spec.getInstructorId());
  }

  private Long countStudentsBySpec(DashboardInstructorSpecification spec) {
    if (spec.hasGroupFilter()) {
      String query =
        "SELECT COUNT(DISTINCT s.id) FROM Group g " +
        "JOIN g.students s " +
        "WHERE g.instructor.id = :instructorId " +
        "AND g.id = :groupId " +
        "AND g.deletedTimestamp IS NULL";
      return entityManager
        .createQuery(query, Long.class)
        .setParameter("instructorId", spec.getInstructorId())
        .setParameter("groupId", spec.getGroupId())
        .getSingleResult();
    }
    return countStudentsByInstructor(spec.getInstructorId());
  }

  private Long countExamsBySpec(DashboardInstructorSpecification spec) {
    if (spec.hasGroupFilter()) {
      String query =
        "SELECT COUNT(DISTINCT e.id) FROM Group g " +
        "JOIN g.groupExams ge " +
        "JOIN ge.exam e " +
        "WHERE g.instructor.id = :instructorId " +
        "AND g.id = :groupId " +
        "AND g.deletedTimestamp IS NULL " +
        "AND e.deletedTimestamp IS NULL";
      return entityManager
        .createQuery(query, Long.class)
        .setParameter("instructorId", spec.getInstructorId())
        .setParameter("groupId", spec.getGroupId())
        .getSingleResult();
    }
    return countExamsByInstructor(spec.getInstructorId());
  }

  private Long countExercisesBySpec(DashboardInstructorSpecification spec) {
    if (spec.hasGroupFilter()) {
      String query =
        "SELECT COUNT(ge.exercise_id) " +
        "FROM group_exercises ge " +
        "WHERE ge.group_id = ?1 " +
        "AND EXISTS (" +
        "  SELECT 1 FROM groups g " +
        "  WHERE g.id = ?1 " +
        "  AND g.owner_id = ?2 " +
        "  AND g.deleted_timestamp IS NULL" +
        ")";
      return (
        (Number) entityManager
          .createNativeQuery(query)
          .setParameter(1, spec.getGroupId())
          .setParameter(2, spec.getInstructorId())
          .getSingleResult()
      ).longValue();
    }
    return countExercisesByInstructor(spec.getInstructorId());
  }

  private Long countExamsComingTodayBySpec(DashboardInstructorSpecification spec) {
    LocalDate today = LocalDate.now();
    ZonedDateTime startOfDay = today.atStartOfDay(ZoneId.systemDefault());
    ZonedDateTime endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault());

    Instant startInstant = startOfDay.toInstant();
    Instant endInstant = endOfDay.toInstant();

    if (spec.hasGroupFilter()) {
      String query =
        "SELECT COUNT(DISTINCT e.id) FROM Group g " +
        "JOIN g.groupExams ge " +
        "JOIN ge.exam e " +
        "WHERE g.instructor.id = :instructorId " +
        "AND g.id = :groupId " +
        "AND g.deletedTimestamp IS NULL " +
        "AND e.deletedTimestamp IS NULL " +
        "AND e.startTime >= :startOfDay " +
        "AND e.startTime < :endOfDay";

      return entityManager
        .createQuery(query, Long.class)
        .setParameter("instructorId", spec.getInstructorId())
        .setParameter("groupId", spec.getGroupId())
        .setParameter("startOfDay", startInstant)
        .setParameter("endOfDay", endInstant)
        .getSingleResult();
    }
    return countExamsComingToday(spec.getInstructorId());
  }
}
