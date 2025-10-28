package com.example.modules.exercises.enums;

import com.example.modules.exercises.exceptions.DifficultyNotFoundException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
  EASY("EASY", 100),
  MEDIUM("MEDIUM", 200),
  HARD("HARD", 300);

  private final String difficulty;
  private final int value;

  public static Difficulty getValue(String difficulty) {
    for (Difficulty d : Difficulty.values()) {
      if (d.getDifficulty().equals(difficulty)) {
        return d;
      }
    }

    throw new DifficultyNotFoundException();
  }
}
