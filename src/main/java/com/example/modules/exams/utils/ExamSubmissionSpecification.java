package com.example.modules.exams.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exams.entities.ExamSubmission;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExamSubmissionSpecification extends SpecificationBuilder<ExamSubmission> {

  public static ExamSubmissionSpecification builder() {
    return new ExamSubmissionSpecification();
  }

  public ExamSubmissionSpecification withExamId(String examId) {
    if (examId != null && !examId.trim().isEmpty()) {
      specifications.add((root, query, cb) -> cb.equal(root.get("exam").get("id"), examId));
    }
    return this;
  }

  public ExamSubmissionSpecification withUserId(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      specifications.add((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
    }
    return this;
  }
}
