package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.ExamSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSubmissionRepository
  extends JpaRepository<ExamSubmission, String>, JpaSpecificationExecutor<ExamSubmission> {
  List<ExamSubmission> findByGroupExamIdAndUserId(String groupExamId, String userId);

  List<ExamSubmission> findByGroupExamId(String groupExamId);

  List<ExamSubmission> findByGroupExamIdAndUserIdAndExerciseId(
    String groupExamId,
    String userId,
    String exerciseId
  );

  // Helper methods để tìm theo examId (qua groupExam.exam.id)
  List<ExamSubmission> findByGroupExam_Exam_Id(String examId);

  List<ExamSubmission> findByGroupExam_Exam_IdAndUserId(String examId, String userId);
}
