package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.Exam;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamRepository
  extends JpaRepository<Exam, String>, JpaSpecificationExecutor<Exam> {
  boolean existsByCode(String code);

  Optional<Exam> findByCode(String code);

  Optional<Exam> findById(String id);
}
