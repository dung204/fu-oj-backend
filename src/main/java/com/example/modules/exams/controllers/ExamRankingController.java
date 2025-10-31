package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.EXAMS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamRankingRequestDto;
import com.example.modules.exams.dtos.ExamRankingResponseDto;
import com.example.modules.exams.services.ExamRankingService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = EXAMS_PREFIX + "/submissions/rank")
@RequiredArgsConstructor
@Tag(name = "Exam Rankings", description = "API to list all submissions (scores) for an exam")
public class ExamRankingController {

  private final ExamRankingService examRankingService;

  @AllowRoles({ Role.INSTRUCTOR, Role.STUDENT, Role.ADMIN })
  @Operation(
    summary = "Get all submissions of an exam",
    description = "Return list of submissions (with scores/metrics) for a given exam",
    responses = {
      @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Exam not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<ExamRankingResponseDto>> getExamSubmissions(
    @ParameterObject @Valid ExamRankingRequestDto dto,
    @CurrentUser User currentUser
  ) {
    log.info("Fetching all exam submissions for exam {}", dto.getExamId());
    List<ExamRankingResponseDto> data = examRankingService.getExamRankings(dto, currentUser);
    return SuccessResponseDTO.<List<ExamRankingResponseDto>>builder()
      .status(200)
      .message("Exam submissions retrieved successfully")
      .data(data)
      .build();
  }
}
