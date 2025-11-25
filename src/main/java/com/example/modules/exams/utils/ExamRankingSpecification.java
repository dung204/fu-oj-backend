package com.example.modules.exams.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exams.entities.ExamRanking;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExamRankingSpecification extends SpecificationBuilder<ExamRanking> {

  public static ExamRankingSpecification builder() {
    return new ExamRankingSpecification();
  }

  public ExamRankingSpecification withGroupExamId(String groupExamId) {
    if (groupExamId != null && !groupExamId.trim().isEmpty()) {
      specifications.add((root, query, cb) ->
        cb.equal(root.get("groupExam").get("id"), groupExamId)
      );
    }
    return this;
  }

  public ExamRankingSpecification withUserId(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      specifications.add((root, query, cb) -> cb.equal(root.get("user").get("id"), userId));
    }
    return this;
  }

  public ExamRankingSpecification withTotalScore(Double totalScore) {
    if (totalScore != null) {
      specifications.add((root, query, cb) -> cb.equal(root.get("totalScore"), totalScore));
    }
    return this;
  }

  public ExamRankingSpecification withMinScore(Double minScore) {
    if (minScore != null) {
      specifications.add((root, query, cb) ->
        cb.greaterThanOrEqualTo(root.get("totalScore"), minScore)
      );
    }
    return this;
  }

  public ExamRankingSpecification withMaxScore(Double maxScore) {
    if (maxScore != null) {
      specifications.add((root, query, cb) ->
        cb.lessThanOrEqualTo(root.get("totalScore"), maxScore)
      );
    }
    return this;
  }
}
