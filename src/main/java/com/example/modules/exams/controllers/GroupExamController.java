package com.example.modules.exams.controllers;

import static com.example.base.utils.AppRoutes.GROUP_EXAMS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exams.dtos.GroupExamRequestDTO;
import com.example.modules.exams.dtos.GroupExamResponseDTO;
import com.example.modules.exams.services.GroupExamService;
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
@RequestMapping(path = GROUP_EXAMS_PREFIX)
@RequiredArgsConstructor
@Tag(name = "group-exams", description = "API for managing group exams")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupExamController {

  GroupExamService groupExamService;

  @Operation(
    summary = "Get group exams with filters",
    description = """
    Lấy danh sách GroupExam với các filters:
    - **groupExamId**: Filter theo ID của chính GroupExam
    - **groupId**: Filter theo group ID
    - **examId**: Filter theo exam ID
    - **ownerId**: Filter theo instructor owner của group
    - **status**: Filter theo status (DRAFT, UPCOMING, ONGOING, COMPLETED, CANCELLED, OUTDATED)

    **Response bao gồm:**
    - GroupExam ID
    - Exam information (id, code, title, time range, etc.)
    - Group information (id, code, name, owner info)
    - Status của GroupExam

    **Access:**
    - All authenticated users can access
    """,
    responses = {
      @ApiResponse(responseCode = "200", description = "Group exams retrieved successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid request format",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<GroupExamResponseDTO>> getGroupExams(
    @ParameterObject @Valid GroupExamRequestDTO dto,
    @CurrentUser User currentUser
  ) {
    log.info(
      "Getting group exams with filters: groupExamId={}, groupId={}, examId={}, ownerId={}, status={}",
      dto.getGroupExamId(),
      dto.getGroupId(),
      dto.getExamId(),
      dto.getOwnerId(),
      dto.getStatus()
    );

    List<GroupExamResponseDTO> groupExams = groupExamService.getGroupExams(dto, currentUser);

    return SuccessResponseDTO.<List<GroupExamResponseDTO>>builder()
      .status(200)
      .message("Lấy danh sách kỳ thi nhóm thành công")
      .data(groupExams)
      .build();
  }

  @AllowRoles({ Role.INSTRUCTOR, Role.ADMIN })
  @Operation(
    summary = "Toggle group exam examined status (for INSTRUCTOR or ADMIN)",
    description = "Toggles the `isExamined` status of a group exam between `true` and `false`.\n\n" +
      "This field indicates whether the exam for this specific group has been reviewed/graded.\n\n" +
      "**Access Control:**\n" +
      "- `INSTRUCTOR`: Can only toggle status for group exams where they own the group\n" +
      "- `ADMIN`: Can toggle status for any group exam",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Group exam examined status toggled successfully"
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Group exam not found or user is not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}/toggle-examined")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupExamResponseDTO> toggleGroupExamExaminedStatus(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<GroupExamResponseDTO>builder()
      .status(200)
      .message("Thay đổi trạng thái chấm điểm kỳ thi nhóm thành công")
      .data(groupExamService.toggleGroupExamExaminedStatus(id, currentUser))
      .build();
  }
}
