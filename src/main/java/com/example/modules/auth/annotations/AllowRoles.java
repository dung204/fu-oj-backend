package com.example.modules.auth.annotations;

import com.example.modules.auth.enums.Role;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to specify which user roles are allowed to access a particular route.
 *
 * <p>
 * By default, all {@link Role}s are permitted.
 * Apply this annotation to controller methods to restrict access based on user roles.
 *
 * For example:
 * <pre>
 * &#64;AllowRoles({Role.ADMIN})
 * public ResponseEntity<?> adminOnlyEndpoint() { ... }
 * </pre>
 *
 * @see Role
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AllowRoles {
  Role[] value() default { Role.ADMIN, Role.USER };
}
