package com.example.base.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

/**
 * A generic success response DTO that extends ResponseDTO to represent successful API responses.
 * This class encapsulates successful HTTP responses with optional data payload.
 *
 * <p>The response includes:
 * <ul>
 *   <li>HTTP status code (defaults to 200 or validated success status)</li>
 *   <li>Success message</li>
 *   <li>Optional data payload of generic type T</li>
 * </ul>
 *
 * @param <T> the type of data payload contained in the response
 *
 * @see ResponseDTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponseDTO<T> extends ResponseDTO {

  private T data;

  @Builder
  private SuccessResponseDTO(Integer status, @NonNull String message, T data) {
    super(status == null ? 200 : validateSuccessStatus(status), message);
    this.data = data;
  }

  public SuccessResponseDTO() {
    super(200, "");
  }
}
