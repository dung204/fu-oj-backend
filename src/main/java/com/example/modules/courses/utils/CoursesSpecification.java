package com.example.modules.courses.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.courses.entities.Course;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoursesSpecification extends SpecificationBuilder<Course> {

  public static CoursesSpecification builder() {
    return new CoursesSpecification();
  }
}
