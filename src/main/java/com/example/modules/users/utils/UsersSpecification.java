package com.example.modules.users.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.users.entities.User;
import jakarta.persistence.criteria.Predicate;

public class UsersSpecification extends SpecificationBuilder<User> {

  public static UsersSpecification builder() {
    return new UsersSpecification();
  }

  public UsersSpecification containsFullNameOrContainsRollNumberOrContainsEmail(String query) {
    if (query != null && !query.isEmpty()) {
      specifications.add((root, criteriaQuery, criteriaBuilder) -> {
        String pattern = "%" + query.toLowerCase() + "%";

        Predicate rollNumberPredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("rollNumber")),
          pattern
        );

        Predicate emailPredicate = criteriaBuilder.like(
          criteriaBuilder.lower(root.get("account").get("email")),
          pattern
        );

        Predicate fullNamePredicate = criteriaBuilder.like(
          criteriaBuilder.lower(
            criteriaBuilder.concat(
              criteriaBuilder.concat(root.get("firstName"), " "),
              root.get("lastName")
            )
          ),
          pattern
        );

        // Combine all predicates with OR
        return criteriaBuilder.or(rollNumberPredicate, emailPredicate, fullNamePredicate);
      });
    }
    return this;
  }

  public UsersSpecification inGroup(String groupId) {
    if (groupId != null && !groupId.trim().isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        query.distinct(true);
        return criteriaBuilder.equal(root.join("joinedGroups").get("id"), groupId);
      });
    }
    return this;
  }
}
