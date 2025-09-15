package com.example.modules.auth.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that authentication is optional for a particular route.
 *
 * <p>
 * Controller methods annotated with {@code @OptionalAuth} can be accessed with or without authentication.
 * Implementations should handle both authenticated and unauthenticated requests accordingly.
 * </p>
 *
 * <p>
 * This annotation is intended to be used on controller methods where authentication is not strictly required.
 * </p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalAuth {}
