package com.example.modules.exercises.enums;

import com.example.modules.exercises.exceptions.VisibilityNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Visibility {
  PUBLIC("PUBLIC", "PUBLIC"),
  PRIVATE("PRIVATE", "PRIVATE"),
  DRAFT("DRAFT", "DRAFT");

  private final String visibility;
  private final String value;

  public static Visibility getValue(String visibility) {
    for (Visibility d : Visibility.values()) {
      if (d.getVisibility().equals(visibility)) {
        return d;
      }
    }

    throw new VisibilityNotFoundException();
  }
}
