package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.ExamExercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamExerciseRepository extends JpaRepository<ExamExercise, String> {
  List<ExamExercise> findByExamId(String examId);
}
