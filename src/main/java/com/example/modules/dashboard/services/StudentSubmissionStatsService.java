package com.example.modules.dashboard.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.dashboard.dtos.StudentSubmissionDashboardResponse;
import com.example.modules.dashboard.dtos.StudentSubmissionStatsRequestDTO;
import com.example.modules.dashboard.dtos.StudentSubmissionStatsResponseDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class StudentSubmissionStatsService {

  @PersistenceContext
  private EntityManager entityManager;

  private final UsersRepository usersRepository;

  /**
   * Lấy thống kê bài nộp của sinh viên
   * - STUDENT: Chỉ xem được thống kê của chính mình
   * - ADMIN: Xem được thống kê của tất cả sinh viên hoặc filter theo studentId
   */
  @Transactional(readOnly = true)
  public StudentSubmissionDashboardResponse getStudentSubmissionStats(
    StudentSubmissionStatsRequestDTO request,
    User currentUser
  ) {
    // 1. Validate request
    validateSubmissionStatsRequest(request, currentUser);

    // 2. Xác định targetStudentId dựa trên role
    String targetStudentId = determineTargetStudentId(request, currentUser);

    // 3. Convert LocalDate sang Instant
    Instant startInstant = request.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant();

    Instant endInstant = request
      .getEndDate()
      .atTime(23, 59, 59)
      .atZone(ZoneId.systemDefault())
      .toInstant();

    // 4. Execute query để lấy stats theo ngày
    List<StudentSubmissionStatsResponseDTO> results = executeSubmissionStatsQuery(
      startInstant,
      endInstant,
      targetStudentId
    );

    // 5. Fill missing dates
    List<StudentSubmissionStatsResponseDTO> filledStats = fillMissingDates(
      results,
      request.getStartDate(),
      request.getEndDate()
    );

    // 6. Tính tổng số bài đã giải trong toàn bộ khoảng thời gian
    Long totalSolved = calculateTotalSolved(startInstant, endInstant, targetStudentId);

    // 7. Return wrapper DTO
    return StudentSubmissionDashboardResponse.builder()
      .totalSolved(totalSolved)
      .stats(filledStats)
      .build();
  }

  private void validateSubmissionStatsRequest(
    StudentSubmissionStatsRequestDTO request,
    User currentUser
  ) {
    Role role = currentUser.getAccount().getRole();

    // Validate startDate và endDate
    if (request.getStartDate() == null) {
      throw new IllegalArgumentException("startDate is required");
    }

    if (request.getEndDate() == null) {
      throw new IllegalArgumentException("endDate is required");
    }

    // Validate startDate <= endDate
    if (request.getStartDate().isAfter(request.getEndDate())) {
      throw new IllegalArgumentException("startDate must be before or equal to endDate");
    }

    // Validate date range không quá lớn (max 90 ngày)
    long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
    if (daysBetween > 90) {
      throw new IllegalArgumentException("Date range cannot exceed 90 days");
    }

    // Validate studentId theo role
    if (role == Role.STUDENT && request.getStudentId() != null) {
      log.warn(
        "STUDENT role cannot use studentId filter, ignoring it. User: {}",
        currentUser.getId()
      );
    }

    if (role == Role.ADMIN && request.getStudentId() != null) {
      // Validate studentId tồn tại và có role STUDENT
      User student = usersRepository
        .findById(request.getStudentId())
        .orElseThrow(() ->
          new UserNotFoundException("Student with ID " + request.getStudentId() + " not found")
        );

      if (student.getAccount().getRole() != Role.STUDENT) {
        throw new IllegalArgumentException("studentId must be a STUDENT user");
      }
    }
  }

  private String determineTargetStudentId(
    StudentSubmissionStatsRequestDTO request,
    User currentUser
  ) {
    Role role = currentUser.getAccount().getRole();

    if (role == Role.STUDENT) {
      // STUDENT: luôn dùng chính mình
      return currentUser.getId();
    } else if (role == Role.ADMIN) {
      // ADMIN: dùng studentId từ request nếu có, null nếu không (tất cả)
      return request.getStudentId();
    } else {
      throw new IllegalArgumentException("Only STUDENT and ADMIN can access this endpoint");
    }
  }

  private List<StudentSubmissionStatsResponseDTO> executeSubmissionStatsQuery(
    Instant startInstant,
    Instant endInstant,
    String studentId
  ) {
    // Sử dụng native query để có thể dùng DATE() function
    String query =
      "SELECT " +
      "  DATE(s.created_timestamp) as date, " +
      "  COUNT(DISTINCT s.exercise_id) as totalSolved, " +
      "  COUNT(DISTINCT CASE " +
      "    WHEN e.difficulty = 'EASY' " +
      "    AND COALESCE(s.score, 0) >= 100 " +
      "    THEN s.exercise_id " +
      "  END) as easy, " +
      "  COUNT(DISTINCT CASE " +
      "    WHEN e.difficulty = 'MEDIUM' " +
      "    AND COALESCE(s.score, 0) >= 200 " +
      "    THEN s.exercise_id " +
      "  END) as medium, " +
      "  COUNT(DISTINCT CASE " +
      "    WHEN e.difficulty = 'HARD' " +
      "    AND COALESCE(s.score, 0) >= 300 " +
      "    THEN s.exercise_id " +
      "  END) as hard " +
      "FROM submissions s " +
      "JOIN exercises e ON s.exercise_id = e.id " +
      "WHERE s.is_accepted = true " +
      "  AND s.is_examination = false " +
      "  AND s.deleted_timestamp IS NULL " +
      "  AND e.deleted_timestamp IS NULL " +
      "  AND s.created_timestamp >= :startInstant " +
      "  AND s.created_timestamp <= :endInstant " +
      "  AND (:studentId IS NULL OR s.user_id = :studentId) " +
      "GROUP BY DATE(s.created_timestamp) " +
      "ORDER BY date ASC";

    @SuppressWarnings("unchecked")
    List<Object[]> results = entityManager
      .createNativeQuery(query)
      .setParameter("startInstant", startInstant)
      .setParameter("endInstant", endInstant)
      .setParameter("studentId", studentId)
      .getResultList();

    List<StudentSubmissionStatsResponseDTO> stats = new ArrayList<>();
    for (Object[] row : results) {
      java.sql.Date dateSql = (java.sql.Date) row[0];
      LocalDate date = dateSql.toLocalDate();
      Long totalSolved = ((Number) row[1]).longValue();
      Long easy = row[2] != null ? ((Number) row[2]).longValue() : 0L;
      Long medium = row[3] != null ? ((Number) row[3]).longValue() : 0L;
      Long hard = row[4] != null ? ((Number) row[4]).longValue() : 0L;

      stats.add(
        StudentSubmissionStatsResponseDTO.builder()
          .date(date.toString())
          .totalSolved(totalSolved)
          .easy(easy)
          .medium(medium)
          .hard(hard)
          .build()
      );
    }

    return stats;
  }

  private List<StudentSubmissionStatsResponseDTO> fillMissingDates(
    List<StudentSubmissionStatsResponseDTO> results,
    LocalDate startDate,
    LocalDate endDate
  ) {
    // Tạo map từ results để dễ lookup
    Map<String, StudentSubmissionStatsResponseDTO> statsMap = new HashMap<>();
    for (StudentSubmissionStatsResponseDTO stat : results) {
      statsMap.put(stat.getDate(), stat);
    }

    // Tạo list đầy đủ các ngày trong range
    List<StudentSubmissionStatsResponseDTO> filledStats = new ArrayList<>();
    LocalDate currentDate = startDate;

    while (!currentDate.isAfter(endDate)) {
      String dateStr = currentDate.toString();
      StudentSubmissionStatsResponseDTO stat = statsMap.get(dateStr);

      if (stat != null) {
        filledStats.add(stat);
      } else {
        // Ngày không có submission, tạo record với giá trị 0
        filledStats.add(
          StudentSubmissionStatsResponseDTO.builder()
            .date(dateStr)
            .totalSolved(0L)
            .easy(0L)
            .medium(0L)
            .hard(0L)
            .build()
        );
      }

      currentDate = currentDate.plusDays(1);
    }

    return filledStats;
  }

  private Long calculateTotalSolved(Instant startInstant, Instant endInstant, String studentId) {
    // Query để tính tổng số bài tập đã giải được trong toàn bộ khoảng thời gian
    String query =
      "SELECT COUNT(DISTINCT s.exercise_id) " +
      "FROM submissions s " +
      "JOIN exercises e ON s.exercise_id = e.id " +
      "WHERE s.is_accepted = true " +
      "  AND s.is_examination = false " +
      "  AND s.deleted_timestamp IS NULL " +
      "  AND e.deleted_timestamp IS NULL " +
      "  AND s.created_timestamp >= :startInstant " +
      "  AND s.created_timestamp <= :endInstant " +
      "  AND (:studentId IS NULL OR s.user_id = :studentId) " +
      "  AND COALESCE(s.score, 0) >= " +
      "    CASE e.difficulty " +
      "      WHEN 'EASY' THEN 100 " +
      "      WHEN 'MEDIUM' THEN 200 " +
      "      WHEN 'HARD' THEN 300 " +
      "    END";

    Object result = entityManager
      .createNativeQuery(query)
      .setParameter("startInstant", startInstant)
      .setParameter("endInstant", endInstant)
      .setParameter("studentId", studentId)
      .getSingleResult();

    return result != null ? ((Number) result).longValue() : 0L;
  }
}
