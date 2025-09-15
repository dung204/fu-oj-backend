package com.example.base.dtos;

import lombok.Builder;
import lombok.Getter;

/**
 * Data Transfer Object representing an error response.
 * <p>
 * This class extends {@link ResponseDTO} to provide a standardized structure
 * for error responses in the API. It encapsulates error information including
 * status code and error message.
 * </p>
 *
 * @see ResponseDTO
 */
@Getter
public class ErrorResponseDTO extends ResponseDTO {

  @Builder
  private ErrorResponseDTO(int status, String message) {
    super(validateErrorStatus(status), message);
  }

  public ErrorResponseDTO() {
    super(400, "");
  }
}
