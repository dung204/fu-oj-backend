package com.example.base.validators;

import com.example.base.annotations.OrderParam;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.stream.Collectors;

public class OrderParamValidator implements ConstraintValidator<OrderParam, String> {

  private List<String> allowedFields;

  @Override
  public void initialize(OrderParam constraintAnnotation) {
    this.allowedFields = List.of(constraintAnnotation.allowedFields());
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (
      value == null ||
      value.trim().isEmpty() ||
      !value.matches("(%s):(asc|desc|ASC|DESC)".formatted(String.join("|", allowedFields)))
    ) {
      context.disableDefaultConstraintViolation();
      context
        .buildConstraintViolationWithTemplate(
          "must be in format 'property:(asc|desc|ASC|DESC)', where `property` accepts: %s".formatted(
              String.join(
                ", ",
                allowedFields
                  .stream()
                  .map(field -> String.format("'%s'", field))
                  .collect(Collectors.toList())
              )
            )
        )
        .addConstraintViolation();
      return false;
    }

    return true;
  }
}
