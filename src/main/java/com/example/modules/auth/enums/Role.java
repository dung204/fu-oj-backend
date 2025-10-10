package com.example.modules.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
  STUDENT("STUDENT"),
  INSTRUCTOR("INSTRUCTOR"),
  ADMIN("ADMIN");

  private final String value;
}
