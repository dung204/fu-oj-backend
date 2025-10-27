package com.example.modules.comments.controllers;

import static com.example.base.utils.AppRoutes.COMMENTS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.comments.dtos.*;
import com.example.modules.comments.services.CommentsService;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = COMMENTS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "comments", description = "Operations related to comments")
@RequiredArgsConstructor
public class CommentsController {

  private final CommentsService commentsService;

  //@AllowRoles({Role.INSTRUCTOR, Role.STUDENT})
  @Operation(
    summary = "Create new comment",
    responses = {
      @ApiResponse(responseCode = "201", description = "Create new comment successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "comment is error format",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Create new comment is fail",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<CommentResponseDTO> createComment(
    @RequestBody @Valid CommentRequestDTO commentRequestDTO,
    @CurrentUser User user
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(201)
      .message("comment created successfully")
      .data(commentsService.createComment(user, commentRequestDTO))
      .build();
  }

  @Operation(
    summary = "Retrieve a list of comments based on exercise ID and parent comment ID",
    description = "Fetches a paginated list of comments for a specific exercise. " +
      "The returned comments depend on the provided exercise ID and optional parent comment ID:\n" +
      "  * If only `exerciseId` is provided, returns all top-level comments for that exercise.\n" +
      "  * If both `exerciseId` and `parentCommentId` are provided, returns all replies to the specified parent comment.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise or comment not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<CommentResponseDTO> getComment(
    @ParameterObject @Valid CommentQueryDTO commentQueryDTO
  ) {
    return PaginatedSuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Get comment successfully")
      .page(commentsService.getCommentsByParentIdAndExerciseId(commentQueryDTO))
      .filters(commentQueryDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Update comment by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Update comment by id successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Update comment by id error",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PutMapping("/{id}")
  public SuccessResponseDTO<CommentResponseDTO> updateCommentById(
    @PathVariable String id,
    @RequestBody String content
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("comment update successfully")
      .data(commentsService.updateCommentById(id, content))
      .build();
  }

  @Operation(
    summary = "Delete comment by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Delete comment by id successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Delete comment by id error",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping
  public SuccessResponseDTO<CommentResponseDTO> deleteCommentById(@RequestBody String commentId) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("comment update successfully")
      .data(commentsService.deleteCommentById(commentId))
      .build();
  }

  @Operation(
    summary = "Report comment by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Report comment by id successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Report comment by id error",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PutMapping("/report/{id}")
  public SuccessResponseDTO<CommentResponseDTO> reportCommentById(@PathVariable String id) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("comment report successfully")
      .data(commentsService.reportCommentById(id, 1))
      .build();
  }
}
