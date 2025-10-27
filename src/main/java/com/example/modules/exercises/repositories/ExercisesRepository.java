package com.example.modules.exercises.repositories;

import com.example.modules.exercises.entities.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExercisesRepository
  extends JpaRepository<Exercise, String>, JpaSpecificationExecutor<Exercise> {
  Exercise findExerciseById(String id);
}
