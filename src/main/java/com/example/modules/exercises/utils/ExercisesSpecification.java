package com.example.modules.exercises.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.exercises.entities.Exercise;

public class ExercisesSpecification extends SpecificationBuilder<Exercise> {

  public static ExercisesSpecification builder() {
    return new ExercisesSpecification();
  }
}
