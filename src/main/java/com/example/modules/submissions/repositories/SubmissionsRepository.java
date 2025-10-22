package com.example.modules.submissions.repositories;

import com.example.modules.submissions.entities.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionsRepository
  extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
  Integer countByUserIdAndExerciseId(String userId, String exerciseId);
}
