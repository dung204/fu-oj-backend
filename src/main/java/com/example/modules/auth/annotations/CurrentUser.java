package com.example.modules.auth.annotations;

import io.swagger.v3.oas.annotations.Parameter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the currently authenticated user into a controller method parameter.
 * <p>
 * This annotation can <b>only be used on method parameters in controller methods</b> to automatically
 * resolve and inject the current user based on the security context.
 * </p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Parameter(hidden = true)
public @interface CurrentUser {}
