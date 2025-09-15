package com.example.base.dtos;

import com.example.base.utils.SwaggerExamples;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import lombok.Data;

/**
 * Abstract base class for all response DTOs in the API.
 *
 * <p>This class provides common fields and validation methods for HTTP responses,
 * including status code, message, and timestamp. It serves as a foundation for
 * both success and error response DTOs.</p>
 *
 * <p>The class includes validation methods to ensure proper HTTP status codes
 * are used for success (2XX-3XX) and error (4XX-5XX) responses.</p>
 *
 */
@Data
public abstract class ResponseDTO {

  @Schema(example = SwaggerExamples.STATUS_CODE, description = "HTTP status code")
  protected int status;

  protected String message;

  @Schema(example = SwaggerExamples.TIMESTAMP, description = "Response timestamp")
  protected String timestamp = Instant.now().toString();

  protected ResponseDTO(int status, String message) {
    this.status = status;
    this.message = message;
  }

  protected static int validateSuccessStatus(int status) {
    if (status < 200 || status > 399) {
      throw new IllegalArgumentException(
        String.format("Invalid success HTTP status code: %s. Should be 2XX or 3XX.", status)
      );
    }
    return status;
  }

  protected static int validateErrorStatus(int status) {
    if (status < 400 || status > 599) {
      throw new IllegalArgumentException(
        String.format("Invalid error HTTP status code: %s. Should be 4XX or 5XX.", status)
      );
    }
    return status;
  }
}
