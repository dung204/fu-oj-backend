package com.example.modules.exercises.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.enums.Visibility;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
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

  public ExercisesSpecification containsCode(String code) {
    if (code != null && !code.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("code")),
          "%" + code.toLowerCase() + "%"
        )
      );
    }
    return this;
  }

  public ExercisesSpecification containsTitle(String title) {
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

  public ExercisesSpecification publicOnly() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.equal(root.get("visibility"), Visibility.PUBLIC.getValue())
    );
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

  public ExercisesSpecification inOneOfGroups(Collection<String> groupIds) {
    specifications.add((root, query, criteriaBuilder) -> {
      query.distinct(true);
      return root.join("groups").get("id").in(groupIds);
    });
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

  public ExercisesSpecification onlyLatestVersion() {
    specifications.add((root, query, cb) -> {
      // Nếu baseId == null: coi như exercise riêng lẻ, lấy luôn
      var baseIdIsNull = cb.isNull(root.get("baseId"));

      // Nếu baseId != null: tìm max version của cùng baseId
      // (bao gồm cả trường hợp baseId == id, vì đó cũng là một nhóm version)
      var subquery = query.subquery(Integer.class);
      var subRoot = subquery.from(Exercise.class);

      subquery.select(cb.max(subRoot.get("version")));
      subquery.where(
        cb.and(
          cb.isNotNull(subRoot.get("baseId")),
          cb.equal(subRoot.get("baseId"), root.get("baseId"))
        )
      );

      var versionEqualsMax = cb.equal(root.get("version"), subquery);
      var baseIdNotNull = cb.isNotNull(root.get("baseId"));

      // Kết hợp: (baseId == null) OR (baseId != null AND version == max version của cùng baseId)
      return cb.or(baseIdIsNull, cb.and(baseIdNotNull, versionEqualsMax));
    });
    return this;
  }
}
