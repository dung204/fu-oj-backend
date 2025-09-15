package com.example.base.annotations;

import com.example.base.validators.FileValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.util.unit.DataUnit;

/**
 * Custom validation annotation for file validation including size and type constraints.
 *
 * <p>This annotation can be applied to parameters, fields, or type uses to validate
 * file objects against specified criteria such as maximum file size and allowed file types.</p>
 *
 * <p>The validation is performed by the {@link FileValidator} class, which handles
 * the actual validation logic and error message generation.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * {@code
 * @File(maxSize = 10, sizeUnit = DataUnit.MEGABYTES, allowedTypes = {"image/*", "application/pdf"})
 * private MultipartFile uploadedFile;
 * }
 * </pre>
 *
 * @see FileValidator
 * @see DataUnit
 */
@Documented
@Constraint(validatedBy = FileValidator.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD, ElementType.TYPE_USE })
@Retention(RetentionPolicy.RUNTIME)
public @interface File {
  /**
   * @apiNote This property is here due to the requirement of {@link Constraint} annotation.
   * The message is generated in {@link FileValidator}. There is no need to specify a message
   * here.
   */
  String message() default "";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  /**
   * Specifies the maximum file size allowed for validation,
   * but does not specify the file unit. To specify the file unit
   * for this size, use {@link FileSize#sizeUnit()}
   *
   * @return the maximum file size
   */
  long maxSize() default 5;

  /**
   * Specifies the unit of measurement for the file size validation.
   * This is used in conjunction with {@link FileSize#maxSize()}
   *
   * @return the data unit to be used for file size measurement
   */
  DataUnit sizeUnit() default DataUnit.MEGABYTES;

  /**
   * Specifies the allowed file types for the file validation.
   * <p>
   * This attribute defines an array of permitted file types or extensions
   * that are acceptable for file upload or processing. If no types are specified
   * (empty array), all file types are allowed by default.
   * </p>
   *
   * <p>
   * This attribute also accepts expressions that captures all formats of a media type.
   * For example: {@code image/*}, {@code video/*}, {@code audio/*}
   * </p>
   *
   * @return an array of allowed file types/extensions
   */
  String[] allowedTypes() default {};
}
