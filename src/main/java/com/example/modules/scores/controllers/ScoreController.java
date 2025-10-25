package com.example.modules.scores.controllers;

import static com.example.base.utils.AppRoutes.SCORES_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.modules.scores.dtos.ScoreResponseDTO;
import com.example.modules.scores.dtos.ScoresSearchDTO;
import com.example.modules.scores.services.ScoresService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = SCORES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "scores", description = "Operations related to scores")
@RequiredArgsConstructor
public class ScoreController {

  private final ScoresService scoresService;

  @Operation(
    summary = "Retrieve all existing scores",
    responses = {
      @ApiResponse(responseCode = "200", description = "Scores retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<ScoreResponseDTO> getAllScores(
    @ParameterObject @Valid ScoresSearchDTO scoresSearchDTO
  ) {
    return PaginatedSuccessResponseDTO.<ScoreResponseDTO>builder()
      .message("Scores retrieved successfully.")
      .page(scoresService.getAllScores(scoresSearchDTO))
      .filters(scoresSearchDTO.getFilters())
      .build();
  }
}
