package com.example.base.annotations;

import com.example.base.aspect.TurnstileAspect;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.*;

/**
 * Marks a method to trigger Cloudflare Turnstile verification before its execution.
 * <p>
 * This annotation should be applied to controller methods that handle sensitive operations
 * like user registration, login, or form submissions, to protect them from bots.
 * </p>

 * <p>
 * {@link TurnstileAspect} will detect this annotation at runtime. It will then
 * extract the Turnstile response token from the incoming HTTP request and validate it
 * against the Cloudflare API. If the validation fails, the request will be rejected,
 * typically by throwing an exception, and the annotated method will not be executed.
 * If the validation is successful, the method execution proceeds as normal.
 * </p>
 *
 * @see <a href="https://developers.cloudflare.com/turnstile/">Cloudflare Turnstile Documentation</a>
 * @see TurnstileAspect
 */
@Target(ElementType.METHOD) // Can be applied to methods
@Retention(RetentionPolicy.RUNTIME) // exists at runtime
@Parameter(
  name = "cf-turnstile-response",
  description = "The Cloudflare Turnstile token obtained from the frontend widget after a successful challenge.",
  in = ParameterIn.HEADER,
  required = true,
  schema = @Schema(type = "string")
)
@Documented
public @interface VerifyTurnstile {}
