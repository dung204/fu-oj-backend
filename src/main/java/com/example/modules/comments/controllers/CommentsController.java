package com.example.modules.comments.controllers;

import static com.example.base.utils.AppRoutes.COMMENTS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.services.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = COMMENTS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "comments", description = "Operations related to comments")
@RequiredArgsConstructor
public class CommentsController {

  private final CommentsService commentsService;

  @Operation(
    summary = "Retrieve a list of comments based on exercise ID and parent comment ID",
    description = "Fetches a paginated list of comments for a specific exercise. " +
      "The returned comments depend on the provided exercise ID and optional parent comment ID:\n" +
      "  * If `parentId = 'null'` (string with content of 'null'), returns all top-level comments (of the exercise with `exerciseId` if provided).\n" +
      "  * If `parentId` is provided, returns all replies to the specified parent comment.",
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
    summary = "Get a single comment by its ID",
    description = "Retrieves the details of a specific comment using its unique identifier.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Comment not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  public SuccessResponseDTO<CommentResponseDTO> getCommentById(@PathVariable String id) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Get comment successfully")
      .data(commentsService.getCommentById(id))
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
