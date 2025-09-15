package com.example.base.annotations;

import com.example.base.validators.OrderParamValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for order parameters in REST API endpoints.
 * This annotation validates that order/sort parameters contain only allowed field names
 * and follow the expected format for ordering operations.
 *
 * <p>The annotation can be applied to parameters, fields, or type uses to ensure that
 * ordering requests are restricted to predefined allowed fields, preventing potential
 * security issues and invalid field references.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @GetMapping("/users")
 * public List<User> getUsers(@OrderParam(allowedFields = {"name", "email", "createdAt"}) String order) {
 *     // Implementation
 * }
 * }
 * </pre>
 *
 * @see OrderParamValidator
 */
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OrderParamValidator.class)
public @interface OrderParam {
  /**
   * @apiNote This property is here due to the requirement of {@link Constraint} annotation.
   * The message is generated in {@link OrderParamValidator}. There is no need to specify a message
   * here.
   */
  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] allowedFields() default {};
}
