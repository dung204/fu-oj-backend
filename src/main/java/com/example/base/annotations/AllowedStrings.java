package com.example.base.annotations;

import com.example.base.validators.AllowedStringsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AllowedStringsValidator.class)
public @interface AllowedStrings {
  /**
   * The array of allowed string values.
   */
  String[] values();

  String message() default "is not a valid value";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
