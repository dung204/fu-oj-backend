package com.example.modules.groups.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.groups.entities.Group;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GroupsSpecification extends SpecificationBuilder<Group> {

  public static GroupsSpecification builder() {
    return new GroupsSpecification();
  }

  public GroupsSpecification containsName(String name) {
    if (name != null && !name.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.like(
          criteriaBuilder.lower(root.get("name")),
          "%" + name.toLowerCase() + "%"
        )
      );
    }
    return this;
  }

  public GroupsSpecification publicOnly() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.isTrue(root.get("isPublic"))
    );
    return this;
  }

  public GroupsSpecification privateOnly() {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.isFalse(root.get("isPublic"))
    );
    return this;
  }

  public GroupsSpecification ownedBy(String instructorId) {
    specifications.add((root, query, criteriaBuilder) ->
      criteriaBuilder.equal(root.get("instructor").get("id"), instructorId)
    );
    return this;
  }

  public GroupsSpecification joinedBy(String studentId) {
    if (studentId != null && !studentId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        query.distinct(true);
        return criteriaBuilder.equal(root.join("students").get("id"), studentId);
      });
    }
    return this;
  }

  public GroupsSpecification withCode(String code) {
    if (code != null && !code.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get("code"), code)
      );
    }
    return this;
  }

  public GroupsSpecification joinedByOrPublicOnly(String studentId) {
    if (studentId != null && !studentId.trim().isEmpty()) {
      Specification<Group> isPublicSpec = (root, query, criteriaBuilder) ->
        criteriaBuilder.isTrue(root.get("isPublic"));

      Specification<Group> joinedBySpec = (root, query, criteriaBuilder) -> {
        query.distinct(true);
        return criteriaBuilder.equal(root.join("students").get("id"), studentId);
      };

      specifications.add(isPublicSpec.or(joinedBySpec));
    }
    return this;
  }

  public GroupsSpecification ownedByOrPublicOnly(String instructorId) {
    if (instructorId != null && !instructorId.trim().isEmpty()) {
      Specification<Group> isPublicSpec = (root, query, criteriaBuilder) ->
        criteriaBuilder.isTrue(root.get("isPublic"));

      Specification<Group> ownedBySpec = (root, query, criteriaBuilder) -> {
        query.distinct(true);
        return criteriaBuilder.equal(root.join("instructor").get("id"), instructorId);
      };

      specifications.add(isPublicSpec.or(ownedBySpec));
    }
    return this;
  }
}
