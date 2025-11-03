package com.example.modules.exams.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exams.entities.Exam;
import jakarta.persistence.criteria.Predicate;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExamsSpecification extends SpecificationBuilder<Exam> {

  public static ExamsSpecification builder() {
    return new ExamsSpecification();
  }

  public ExamsSpecification containsCodeOrContainsTitle(String query) {
    if (query != null && !query.isEmpty()) {
      specifications.add((root, criteriaQuery, criteriaBuilder) -> {
        String pattern = "%" + query.toLowerCase() + "%";

        Predicate codePredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("code")),
          pattern
        );
        Predicate titlePredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("title")),
          pattern
        );

        return criteriaBuilder.or(codePredicate, titlePredicate);
      });
    }
    return this;
  }

  public ExamsSpecification belongsToGroups(Collection<String> groupIds) {
    if (groupIds != null && !groupIds.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        root.join("group").get("id").in(groupIds)
      );
    }
    return this;
  }

  public ExamsSpecification withGroupId(String groupId) {
    if (groupId != null && !groupId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) -> {
        var groupJoin = root.join("group");
        return criteriaBuilder.equal(groupJoin.get("id"), groupId);
      });
    }
    return this;
  }

  public ExamsSpecification isOneOfStatuses(Collection<String> statuses) {
    if (statuses != null && !statuses.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> root.get("status").in(statuses));
    }
    return this;
  }
}
