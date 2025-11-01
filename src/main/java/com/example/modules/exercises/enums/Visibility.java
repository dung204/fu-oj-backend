package com.example.modules.exercises.enums;

import com.example.modules.exercises.exceptions.VisibilityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Visibility {
  PUBLIC("PUBLIC"),
  PRIVATE("PRIVATE"),
  DRAFT("DRAFT");

  private final String value;

  public static Visibility getValue(String visibility) {
    for (Visibility d : Visibility.values()) {
      if (d.getValue().equals(visibility)) {
        return d;
      }
    }

    throw new VisibilityNotFoundException();
  }
}
