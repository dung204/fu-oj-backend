package com.example.base.annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // Can be applied to methods
@Retention(RetentionPolicy.RUNTIME) // exists at runtime
@Documented
public @interface VerifyTurnstile {}
