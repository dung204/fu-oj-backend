package com.example.modules.submission_results.repositories;

import com.example.modules.submission_results.entities.SubmissionResult;
import com.example.modules.submissions.entities.Submission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SubmissionResultRepository
  extends JpaRepository<SubmissionResult, String>, JpaSpecificationExecutor<SubmissionResult> {
  Optional<SubmissionResult> findByToken(String token);
  List<SubmissionResult> findAllBySubmission(Submission submission);
}
