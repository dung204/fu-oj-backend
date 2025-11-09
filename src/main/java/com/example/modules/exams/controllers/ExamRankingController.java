package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.EXAM_RANKINGS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.ExamRankingCreateDTO;
import com.example.modules.exams.dtos.ExamRankingRequestDTO;
import com.example.modules.exams.dtos.ExamRankingResponseDTO;
import com.example.modules.exams.services.ExamRankingService;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = EXAM_RANKINGS_PREFIX)
@RequiredArgsConstructor
@Tag(name = "exam-rankings", description = "API for managing exam rankings")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ExamRankingController {

  ExamRankingService examRankingService;

  @Operation(
    summary = "Create exam ranking (for INSTRUCTOR and ADMIN)",
    description = "Tạo ExamRanking với chỉ exam và user. Các field điểm (totalScore, numberOfExercises, numberOfCompletedExercises) sẽ là null.\n\n" +
      "Scheduler sẽ tự động tính toán và cập nhật điểm khi student submit bài.\n\n" +
      "Nếu ExamRanking đã tồn tại cho cặp (exam, user), API sẽ trả về ExamRanking hiện tại.",
    responses = {
      @ApiResponse(responseCode = "201", description = "ExamRanking created successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid request format",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
      @ApiResponse(
        responseCode = "404",
        description = "Exam or User not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<ExamRankingResponseDTO> createExamRanking(
    @RequestBody @Valid ExamRankingCreateDTO dto,
    @CurrentUser User currentUser
  ) {
    ExamRankingResponseDTO ranking = examRankingService.createExamRanking(dto, currentUser);
    return SuccessResponseDTO.<ExamRankingResponseDTO>builder()
      .status(201)
      .message("ExamRanking created successfully")
      .data(ranking)
      .build();
  }

  @Operation(
    summary = "Get exam rankings",
    description = "Lấy danh sách rankings của exam.\n\n" +
      "**STUDENT:** Chỉ xem được ranking của chính mình.\n\n" +
      "**INSTRUCTOR/ADMIN:** Xem được tất cả rankings.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Rankings retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<ExamRankingResponseDTO>> getExamRankings(
    @ParameterObject @Valid ExamRankingRequestDTO dto,
    @CurrentUser User currentUser
  ) {
    List<ExamRankingResponseDTO> rankings = examRankingService.getExamRankings(dto, currentUser);
    return SuccessResponseDTO.<List<ExamRankingResponseDTO>>builder()
      .status(200)
      .message("Rankings retrieved successfully")
      .data(rankings)
      .build();
  }
}
