package com.example.modules.test_cases.repositories;

import com.example.modules.test_cases.entities.TestCase;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface TestCasesRepository
  extends JpaRepository<TestCase, String>, JpaSpecificationExecutor<TestCase> {
  List<TestCase> findAllByExerciseId(String exerciseId);
}
