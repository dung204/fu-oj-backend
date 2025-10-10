package com.example.modules.submission_results.repositories;

import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.entities.Submission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionResultRepository
  extends JpaRepository<SubmissionResult, String>, JpaSpecificationExecutor<SubmissionResult> {
  Optional<SubmissionResult> findByToken(String token);
  List<SubmissionResult> findAllBySubmission(Submission submission);

  @Query(
    "SELECT sr FROM SubmissionResult sr LEFT JOIN FETCH sr.testCase WHERE sr.submission.id = :submissionId"
  )
  List<SubmissionResult> findAllBySubmissionId(@Param("submissionId") String submissionId);

  @Query(
    "SELECT sr FROM SubmissionResult sr LEFT JOIN FETCH sr.testCase WHERE sr.verdict IN :verdicts"
  )
  List<SubmissionResult> findByVerdictIn(@Param("verdicts") List<String> verdicts);
}
