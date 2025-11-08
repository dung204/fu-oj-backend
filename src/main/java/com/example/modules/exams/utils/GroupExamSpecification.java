package com.example.modules.exams.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exams.entities.GroupExam;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupExamSpecification extends SpecificationBuilder<GroupExam> {

  public static GroupExamSpecification builder() {
    return new GroupExamSpecification();
  }

  public GroupExamSpecification withExamId(String examId) {
    if (examId != null && !examId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("exam").get("id"), examId)
      );
    }
    return this;
  }

  public GroupExamSpecification withGroupId(String groupId) {
    if (groupId != null && !groupId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("group").get("id"), groupId)
      );
    }
    return this;
  }

  public GroupExamSpecification withStatus(String status) {
    if (status != null && !status.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("status"), status)
      );
    }
    return this;
  }

  public GroupExamSpecification withExamEndTimeSmallerThanOrEqualTo(Instant instant) {
    if (instant != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get("exam").get("endTime"), instant)
      );
    }
    return this;
  }

  public GroupExamSpecification withExamEndTimeGreaterThan(Instant instant) {
    if (instant != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThan(root.get("exam").get("endTime"), instant)
      );
    }
    return this;
  }

  public GroupExamSpecification withExamStartTimeSmallerThanOrEqualTo(Instant instant) {
    if (instant != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get("exam").get("startTime"), instant)
      );
    }
    return this;
  }
}
