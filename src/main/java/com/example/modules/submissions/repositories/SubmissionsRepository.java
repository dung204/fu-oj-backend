package com.example.modules.submissions.repositories;

import com.example.modules.submissions.entities.Submission;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionsRepository
  extends JpaRepository<Submission, String>, JpaSpecificationExecutor<Submission> {
  Integer countByUserIdAndExerciseId(String userId, String exerciseId);

  @Query(
    "SELECT COUNT(DISTINCT s.exercise.id) " +
      "FROM Submission s " +
      "WHERE s.user.id = :userId " +
      "AND s.exercise.id IN :exerciseIds " +
      "AND s.isAccepted = true"
  )
  long countDistinctAcceptedExercisesForUser(
    @Param("userId") String userId,
    @Param("exerciseIds") Collection<String> exerciseIds
  );
}
