package com.example.modules.test_cases.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.test_cases.entities.TestCase;
import org.springframework.data.jpa.domain.Specification;

public class TestCasesSpecification extends SpecificationBuilder<TestCase> {

  public static TestCasesSpecification builder() {
    return new TestCasesSpecification();
  }

  public TestCasesSpecification withExerciseId(String exerciseId) {
    if (exerciseId != null && !exerciseId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("exercise").get("id"), exerciseId)
      );
    }
    return this;
  }

  public TestCasesSpecification withIsPublic(Boolean isPublic) {
    if (isPublic != null) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("isPublic"), isPublic)
      );
    }
    return this;
  }

  public static Specification<TestCase> buildFilters(String exerciseId, Boolean isPublic) {
    return (root, query, cb) -> {
      var predicates = cb.conjunction();

      if (exerciseId != null && !exerciseId.isEmpty()) {
        predicates.getExpressions().add(cb.equal(root.get("exercise").get("id"), exerciseId));
      }

      if (isPublic != null) {
        predicates.getExpressions().add(cb.equal(root.get("isPublic"), isPublic));
      }

      // Filter deletedTimestamp == null
      var notDeletedPredicate = cb.isNull(root.get("deletedTimestamp"));
      return cb.and(predicates, notDeletedPredicate);
    };
  }
}
