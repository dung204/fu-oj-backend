package com.example.base.validators;

import com.example.base.annotations.AllowedStrings;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class AllowedStringsValidator implements ConstraintValidator<AllowedStrings, String> {

  private Set<String> allowedValues;

  @Override
  public void initialize(AllowedStrings constraintAnnotation) {
    // Convert the array of allowed values into a Set for efficient lookups.
    this.allowedValues = Arrays.stream(constraintAnnotation.values()).collect(Collectors.toSet());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // Null values are considered valid. Use @NotBlank for non-null checks.
    if (value == null) {
      return true;
    }

    boolean isValid = allowedValues.contains(value);

    // If not valid, customize the error message to be more helpful.
    if (!isValid) {
      context.disableDefaultConstraintViolation();
      String message = String.format(
        "'%s' is not a valid value. Allowed values are: %s",
        value,
        allowedValues
      );
      context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }

    return isValid;
  }
}
