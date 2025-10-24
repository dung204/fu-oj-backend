package com.example.modules.exercises.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exercises.entities.Exercise;
import jakarta.persistence.criteria.Predicate;
import java.util.Collection;

public class ExercisesSpecification extends SpecificationBuilder<Exercise> {

  public static ExercisesSpecification builder() {
    return new ExercisesSpecification();
  }

  public ExercisesSpecification withCode(String code) {
    if (code != null && !code.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("code"), code)
      );
    }
    return this;
  }

  public ExercisesSpecification withTitleLike(String title) {
    if (title != null && !title.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("title")),
          "%" + title.toLowerCase() + "%"
        )
      );
    }
    return this;
  }

  public ExercisesSpecification withGroupId(String groupId) {
    if (groupId != null && !groupId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) -> {
        // Join với bảng group_exercises để lọc exercises thuộc group
        var groupJoin = root.join("groups"); // Cần thêm field này vào Exercise entity
        return criteriaBuilder.equal(groupJoin.get("id"), groupId);
      });
    }
    return this;
  }

  public ExercisesSpecification containsCodeOrContainsTitle(String query) {
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

  public ExercisesSpecification hasOneOfTopics(Collection<String> topicIds) {
    if (topicIds != null && !topicIds.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        query.distinct(true);
        return root.join("topics").get("id").in(topicIds);
      });
    }
    return this;
  }
}
