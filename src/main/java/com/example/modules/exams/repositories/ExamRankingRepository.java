package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.ExamRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ExamRankingRepository
  extends JpaRepository<ExamRanking, String>, JpaSpecificationExecutor<ExamRanking> {}
