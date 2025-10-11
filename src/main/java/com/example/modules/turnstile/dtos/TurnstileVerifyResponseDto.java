package com.example.modules.turnstile.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TurnstileVerifyResponseDto {

  private boolean success;

  @JsonProperty("challenge_ts")
  private String challengeTs;

  private String hostname;

  @JsonProperty("error-codes")
  private String[] errorCodes;
}
