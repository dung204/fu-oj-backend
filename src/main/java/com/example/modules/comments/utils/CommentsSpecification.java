package com.example.modules.comments.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.comments.entities.Comment;
import jakarta.persistence.criteria.JoinType;
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
        criteriaBuilder.equal(root.join("exercise", JoinType.LEFT).get("id"), exerciseId)
      );
    }
    return this;
  }
}
