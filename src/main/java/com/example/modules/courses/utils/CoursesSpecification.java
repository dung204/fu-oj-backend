package com.example.modules.courses.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.courses.entities.Course;
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoursesSpecification extends SpecificationBuilder<Course> {

  public static CoursesSpecification builder() {
    return new CoursesSpecification();
  }

  public CoursesSpecification fetchEnrolledStudents() {
    specifications.add((root, query, criteriaBuilder) -> {
      // This check is important to avoid adding the fetch multiple times,
      // especially in count queries where it's not needed and can cause errors.
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        root.fetch("enrolledStudents", JoinType.LEFT);
      }
      // We return an empty predicate because this specification is only for fetching, not filtering.
      return criteriaBuilder.conjunction();
    });
    return this;
  }

  public CoursesSpecification fetchExercises() {
    specifications.add((root, query, criteriaBuilder) -> {
      // This check is important to avoid adding the fetch multiple times,
      // especially in count queries where it's not needed and can cause errors.
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        root.fetch("exercises", JoinType.LEFT);
      }
      // We return an empty predicate because this specification is only for fetching, not filtering.
      return criteriaBuilder.conjunction();
    });
    return this;
  }

  public CoursesSpecification containsTitle(String title) {
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

  public CoursesSpecification withExerciseId(String exerciseId) {
    if (exerciseId != null && !exerciseId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("exercises").get("id"), exerciseId)
      );
    }
    return this;
  }

  public CoursesSpecification withEnrolledStudentId(String studentId) {
    if (studentId != null && !studentId.isBlank()) {
      specifications.add((root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.join("enrolledStudents").get("id"), studentId)
      );
    }
    return this;
  }
}
