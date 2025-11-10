package com.example.modules.exams.repositories;

import com.example.modules.exams.entities.GroupExam;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupExamRepository
  extends JpaRepository<GroupExam, String>, JpaSpecificationExecutor<GroupExam> {
  List<GroupExam> findByExamId(String examId);
  List<GroupExam> findByGroupId(String groupId);
  List<GroupExam> findByExamIdAndGroupId(String examId, String groupId);
}
