package com.example.modules.auth.annotations;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is publicly accessible and does not require authentication.
 *
 * This annotation can be used on controller methods to mark them as open endpoints,
 * bypassing security requirements that may be enforced globally.
 *
 * Usage example:
 * <pre>
 * &#64;Public
 * &#64;GetMapping("/public-endpoint")
 * public ResponseEntity&lt;String&gt; publicEndpoint() {
 *     return ResponseEntity.ok("This endpoint is public.");
 * }
 * </pre>
 *
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@SecurityRequirements
public @interface Public {}
