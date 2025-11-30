package com.example.modules.exercises.services;

import com.example.modules.exercises.entities.Exercise;
import com.example.modules.submissions.entities.Submission;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

interface SubmissionRepository extends Repository<Submission, String> {
  List<Submission> getSubmissionByExercise(Exercise exercise);

  @Query(
    """
      SELECT s.exercise.id
      FROM Submission s
      WHERE s.createdBy = :userId
        AND s.isExamination = false
        AND s.deletedTimestamp IS NULL
        AND s.exercise.id IN :exerciseIds
      GROUP BY s.exercise.id, s.exercise.difficulty
      HAVING MAX(COALESCE(s.score, 0)) >=
        CASE s.exercise.difficulty
          WHEN com.example.modules.exercises.enums.Difficulty.EASY THEN 100
          WHEN com.example.modules.exercises.enums.Difficulty.MEDIUM THEN 200
          WHEN com.example.modules.exercises.enums.Difficulty.HARD THEN 300
        END
    """
  )
  List<String> findSolvedExerciseIdsByUserIdAndExerciseIds(
    @Param("userId") String userId,
    @Param("exerciseIds") Collection<String> exerciseIds
  );
}
