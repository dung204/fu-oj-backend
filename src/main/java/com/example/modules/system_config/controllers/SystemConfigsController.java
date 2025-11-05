package com.example.modules.system_config.controllers;

import static com.example.base.utils.AppRoutes.SYSTEM_CONFIGS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.system_config.dtos.SystemConfigsResponseDTO;
import com.example.modules.system_config.dtos.SystemConfigsUpdateDTO;
import com.example.modules.system_config.services.SystemConfigsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = SYSTEM_CONFIGS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "system-configs", description = "Operations related to system configurations")
@RequiredArgsConstructor
public class SystemConfigsController {

  private final SystemConfigsService systemConfigsService;

  @Operation(
    summary = "Retrieve system configuration",
    description = "Fetches the current system configuration values, including scoring and bonus settings.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "System configurations retrieved successfully",
        content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = SystemConfigsResponseDTO.class)
        )
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Bad request — invalid parameters or request format",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content),
    }
  )
  @GetMapping
  public SuccessResponseDTO<SystemConfigsResponseDTO> getSystemConfigs() {
    SystemConfigsResponseDTO systemConfigs = systemConfigsService.getSystemConfigs();
    return SuccessResponseDTO.<SystemConfigsResponseDTO>builder()
      .status(200)
      .message("System configurations retrieved successfully")
      .data(systemConfigs)
      .build();
  }

  @Operation(
    summary = "Update system configuration by ID",
    description = "Updates an existing system configuration identified by its ID. " +
      "Allows modifying values such as scoring and bonus settings.",
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
      description = "System configuration fields to update",
      required = true,
      content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = SystemConfigsUpdateDTO.class)
      )
    ),
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "System configuration updated successfully",
        content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = SystemConfigsResponseDTO.class)
        )
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Bad request — invalid data or request format",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "System configuration not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content),
    }
  )
  @PutMapping("/{id}")
  public SuccessResponseDTO<SystemConfigsResponseDTO> updateSystemConfigs(
    @RequestBody SystemConfigsUpdateDTO systemConfigsUpdateDTO,
    @PathVariable String id
  ) {
    SystemConfigsResponseDTO updatedSystemConfigs = systemConfigsService.updateSystemConfigs(
      systemConfigsUpdateDTO,
      id
    );
    return SuccessResponseDTO.<SystemConfigsResponseDTO>builder()
      .status(200)
      .message("System configurations updated successfully")
      .data(updatedSystemConfigs)
      .build();
  }
}
