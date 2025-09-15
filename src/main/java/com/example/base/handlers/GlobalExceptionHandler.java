package com.example.base.handlers;

import com.example.base.dtos.ErrorResponseDTO;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponseDTO> handleHttpMethodNotSupportedException(
    HttpRequestMethodNotSupportedException e
  ) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
      ErrorResponseDTO.builder()
        .status(HttpStatus.METHOD_NOT_ALLOWED.value())
        .message(e.getMessage())
        .build()
    );
  }

  @ExceptionHandler({ JwtException.class, IllegalArgumentException.class })
  public ResponseEntity<ErrorResponseDTO> handleJwtException(Exception e) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      ErrorResponseDTO.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .message(e.getMessage())
        .build()
    );
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<ErrorResponseDTO> handleMissingServletRequestPartException(
    MissingServletRequestPartException e
  ) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      ErrorResponseDTO.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .message(e.getMessage())
        .build()
    );
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<ErrorResponseDTO> handleMaxUploadSizeExceededException(
    MaxUploadSizeExceededException e
  ) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      ErrorResponseDTO.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .message(e.getMessage())
        .build()
    );
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponseDTO> handleHandlerMethodValidationException(
    HandlerMethodValidationException e
  ) {
    String message = e
      .getParameterValidationResults()
      .stream()
      .flatMap(result -> result.getResolvableErrors().stream())
      .findFirst()
      .map(MessageSourceResolvable::getDefaultMessage)
      .orElse("Validation failed");

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      ErrorResponseDTO.builder().status(HttpStatus.BAD_REQUEST.value()).message(message).build()
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> handleInvalidBodyFieldException(
    MethodArgumentNotValidException e
  ) {
    final FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);
    final String fieldName = fieldError.getField();
    final String errorMessage = fieldError.getDefaultMessage();

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      ErrorResponseDTO.builder()
        .status(HttpStatus.BAD_REQUEST.value())
        .message("Field `%s` %s".formatted(fieldName, errorMessage))
        .build()
    );
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorResponseDTO> handleHttpException(ResponseStatusException e) {
    if (e.getStatusCode().value() >= 500) {
      log.error("Unknown error: {}", e);
      return ResponseEntity.status(e.getStatusCode()).body(
        ErrorResponseDTO.builder()
          .status(e.getStatusCode().value())
          .message("Unknown error")
          .build()
      );
    }

    return ResponseEntity.status(e.getStatusCode()).body(
      ErrorResponseDTO.builder().status(e.getStatusCode().value()).message(e.getReason()).build()
    );
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> handleUnknownException(Exception e) {
    log.error("Unknown error: {}", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
      ErrorResponseDTO.builder()
        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .message("Unknown error")
        .build()
    );
  }
}
