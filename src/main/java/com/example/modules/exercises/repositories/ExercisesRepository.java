package com.example.modules.exercises.repositories;

import com.example.modules.exercises.entities.Exercise;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ExercisesRepository
  extends JpaRepository<Exercise, String>, JpaSpecificationExecutor<Exercise> {
  Exercise findExerciseById(String id);
  boolean existsByCode(String code);

  Optional<Exercise> findTopByBaseIdOrderByVersionDesc(String baseId);

  @Query("SELECT e.id FROM Exercise e WHERE e.id IN :inputIds AND e.visibility = 'PUBLIC'")
  Set<String> findValidPublicIds(@Param("inputIds") Collection<String> inputIds);
}
