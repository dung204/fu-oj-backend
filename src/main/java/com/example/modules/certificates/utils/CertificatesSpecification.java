package com.example.modules.certificates.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.certificates.entities.Certificate;
import java.util.Collection;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificatesSpecification extends SpecificationBuilder<Certificate> {

  public static CertificatesSpecification builder() {
    return new CertificatesSpecification();
  }

  public CertificatesSpecification withStudentId(String studentId) {
    if (studentId != null && !studentId.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        return criteriaBuilder.equal(root.get("user").get("id"), studentId);
      });
    }
    return this;
  }

  public CertificatesSpecification withStudentIds(Collection<String> studentIds) {
    if (studentIds != null && !studentIds.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        query.distinct(true);
        return root.get("user").get("id").in(studentIds);
      });
    }
    return this;
  }

  public CertificatesSpecification withCourseId(String courseId) {
    if (courseId != null && !courseId.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        return criteriaBuilder.equal(root.get("course").get("id"), courseId);
      });
    }
    return this;
  }

  public CertificatesSpecification withCourseIds(Collection<String> courseIds) {
    if (courseIds != null && !courseIds.isEmpty()) {
      specifications.add((root, query, criteriaBuilder) -> {
        query.distinct(true);
        return root.get("course").get("id").in(courseIds);
      });
    }
    return this;
  }
}
