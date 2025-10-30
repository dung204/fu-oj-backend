package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.ExamSubmission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSubmissionRepository extends JpaRepository<ExamSubmission, String> {
  List<ExamSubmission> findByExamIdAndUserId(String examId, String userId);

  List<ExamSubmission> findByExamId(String examId);
}
