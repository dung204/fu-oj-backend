package com.example.modules.submissions.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SubmissionsSpecification {

  public static SubmissionsSpecification builder() {
    return new SubmissionsSpecification();
  }
}
