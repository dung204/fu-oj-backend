package com.example.modules.dashboard.services;

import com.example.modules.dashboard.dtos.InstructorDashboardStatsDTO;
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

  @Transactional(readOnly = true)
  public InstructorDashboardStatsDTO getDashboardStats(String instructorId) {
    log.info("Lấy thống kê dashboard cho instructorId: {}", instructorId);
    // 1. Đếm tổng số groups của instructor
    Long totalGroups = countGroupsByInstructor(instructorId);

    // 2. Đếm tổng số students (unique) trong các groups của instructor
    Long totalStudents = countStudentsByInstructor(instructorId);

    // 3. Đếm tổng số exams trong các groups của instructor
    Long totalExams = countExamsByInstructor(instructorId);

    // 4. Đếm tổng số exercises được assign cho các groups của instructor
    Long totalExercises = countExercisesByInstructor(instructorId);

    // 5. Đếm số exams diễn ra trong ngày hôm nay
    Long examsComing = countExamsComingToday(instructorId);

    return InstructorDashboardStatsDTO.builder()
      .totalGroups(totalGroups)
      .totalStudents(totalStudents)
      .totalExams(totalExams)
      .totalExercises(totalExercises)
      .examsComing(examsComing)
      .build();
  }

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
    // Đếm số lượng exercises từ bảng group_exercises của các groups thuộc instructor
    String query =
      "SELECT COUNT(ge.exercise_id) " +
      "FROM group_exercises ge " +
      "WHERE ge.group_id IN (" +
      "  SELECT g.id FROM groups g " +
      "  WHERE g.owner_id = ?1 " +
      "  AND g.deleted_timestamp IS NULL" +
      ")";

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
}
