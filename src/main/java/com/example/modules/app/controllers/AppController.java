package com.example.modules.app.controllers;

import static com.example.base.utils.AppRoutes.API_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = API_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "app", description = "Operations related to the application")
public class AppController {

  @Operation(
    summary = "Check application health status",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Application is healthy",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @Public
  @GetMapping("/health")
  public SuccessResponseDTO<?> checkHealth() {
    return SuccessResponseDTO.builder().message("OK").build();
  }
}
