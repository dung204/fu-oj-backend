package com.example.modules.exercises.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exercises.entities.Exercise;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

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

  public static Specification<Exercise> buildOrFilters(
    String code,
    String title,
    List<String> topicIds,
    String groupId
  ) {
    return (root, query, cb) -> {
      var searchPredicates = cb.disjunction(); // OR cho các điều kiện tìm kiếm
      boolean hasSearchCriteria = false;

      if (code != null && !code.isEmpty()) {
        searchPredicates
          .getExpressions()
          .add(cb.like(cb.lower(root.get("code")), "%" + code.toLowerCase() + "%"));
        hasSearchCriteria = true;
      }

      if (title != null && !title.isEmpty()) {
        searchPredicates
          .getExpressions()
          .add(cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%"));
        hasSearchCriteria = true;
      }

      if (groupId != null) {
        Join<Object, Object> groupJoin = root.join(
          "groups",
          jakarta.persistence.criteria.JoinType.LEFT
        );
        searchPredicates.getExpressions().add(cb.equal(groupJoin.get("id"), groupId));
        hasSearchCriteria = true;
      }

      if (topicIds != null && !topicIds.isEmpty()) {
        Join<Object, Object> topicJoin = root.join(
          "topics",
          jakarta.persistence.criteria.JoinType.LEFT
        );
        CriteriaBuilder.In<Object> inClause = cb.in(topicJoin.get("id"));
        topicIds.forEach(inClause::value);
        searchPredicates.getExpressions().add(inClause);
        hasSearchCriteria = true;
      }

      // Luôn filter deletedTimestamp == null và kết hợp với các điều kiện tìm kiếm bằng AND
      var notDeletedPredicate = cb.isNull(root.get("deletedTimestamp"));

      if (hasSearchCriteria) {
        // (code LIKE ... OR title LIKE ... OR ...) AND deletedTimestamp == null
        return cb.and(searchPredicates, notDeletedPredicate);
      } else {
        // Nếu không có filter nào, chỉ trả về exercise chưa bị xóa
        return notDeletedPredicate;
      }
    };
  }
}
