package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.ExamRanking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRankingRepository
  extends JpaRepository<ExamRanking, String>, JpaSpecificationExecutor<ExamRanking> {
  Optional<ExamRanking> findByGroupExamIdAndUserId(String groupExamId, String userId);

  List<ExamRanking> findByGroupExamId(String groupExamId);
}
