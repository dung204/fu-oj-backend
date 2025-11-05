package com.example.modules.comments.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.comments.entities.Comment;
import com.example.modules.exercises.enums.Visibility;
import jakarta.persistence.criteria.JoinType;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentsSpecification extends SpecificationBuilder<Comment> {

  //query sql
  public static CommentsSpecification builder() {
    return new CommentsSpecification();
  }

  public CommentsSpecification withParentId(String parentId) {
    if ("null".equals(parentId)) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.isNull(root.join("parent", JoinType.LEFT).get("id"))
      );
    }

    if (parentId != null && !parentId.trim().isEmpty() && !parentId.equals("null")) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("parent", JoinType.LEFT).get("id"), parentId)
      );
    }

    return this;
  }

  public CommentsSpecification withExerciseId(String exerciseId) {
    if (exerciseId != null && !exerciseId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("exercise").get("id"), exerciseId)
      );
    }
    return this;
  }

  public CommentsSpecification withPublicExercises() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.equal(root.join("exercise").get("visibility"), Visibility.PUBLIC.getValue())
    );
    return this;
  }

  public CommentsSpecification withExercisesCreatedBy(String userId) {
    if (userId != null && !userId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("exercise").get("createdBy"), userId)
      );
    }
    return this;
  }

  public CommentsSpecification withExercisesOfGroups(Collection<String> groupIds) {
    specifications.add((root, query, criteriaBuilder) -> {
      query.distinct(true);
      return root.join("exercise").join("group").get("id").in(groupIds);
    });
    return this;
  }
}
