package com.example.modules.comments.controllers;

import static com.example.base.utils.AppRoutes.COMMENTS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.comments.dtos.CommentQueryDTO;
import com.example.modules.comments.dtos.CommentResponseDTO;
import com.example.modules.comments.dtos.CommentUpdateDTO;
import com.example.modules.comments.services.CommentsService;
import com.example.modules.comments.utils.CommentMapper;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = COMMENTS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "comments", description = "Operations related to comments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentsController {

  CommentsService commentsService;
  CommentMapper commentMapper;

  @Operation(
    summary = "Retrieve a list of comments for an exercise",
    description = "Fetches a paginated list of comments. Access to the parent exercise is verified first, and the visibility of comments depends on the user's role.\n\n" +
      "**Exercise Access Logic:**\n" +
      "*   `ADMIN`: Can access any non-deleted exercise.\n" +
      "*   `INSTRUCTOR`: Can access public exercises or exercises they created.\n" +
      "*   `STUDENT`: Can access public exercises or exercises belonging to groups they are a member of.\n\n" +
      "**Comment Visibility:**\n" +
      "*   `ADMIN` can view all comments, including soft-deleted ones.\n" +
      "*   Other roles can only view non-deleted comments.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comments retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Exercise not found or user not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<CommentResponseDTO> getComment(
    @ParameterObject @Valid CommentQueryDTO commentQueryDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Get comment successfully")
      .page(commentsService.getCommentsByParentIdAndExerciseId(commentQueryDTO, currentUser))
      .filters(commentQueryDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Get a single comment by its ID",
    description = "Retrieves the details of a specific comment. Access is determined by the user's role:\n\n" +
      "*   **ADMIN**: Can retrieve any comment, including soft-deleted ones.\n" +
      "*   **INSTRUCTOR**: Can retrieve non-deleted comments on non-deleted exercises that are either public or created by them.\n" +
      "*   **STUDENT**: Can retrieve non-deleted comments on non-deleted exercises belonging to a group they are a member of.\n\n" +
      "A `404 Not Found` error is returned if the comment does not exist or if the user is not authorized to view it.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comment retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Comment not found or user not authorized",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  public SuccessResponseDTO<CommentResponseDTO> getCommentById(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Get comment successfully")
      .data(commentMapper.toCommentResponseDTO(commentsService.getCommentById(id, currentUser)))
      .build();
  }

  @Operation(
    summary = "Update a comment by ID",
    description = "Updates the content of a specific comment. Access to the comment is first determined by the user's role, followed by an ownership check.\n\n" +
      "**Access Logic:**\n" +
      "*   `ADMIN`: Can access any comment.\n" +
      "*   `INSTRUCTOR`: Can access non-deleted comments on non-deleted exercises that are either public or created by them.\n" +
      "*   `STUDENT`: Can access non-deleted comments on non-deleted exercises belonging to a group they are a member of.\n\n" +
      "**Ownership Logic:**\n" +
      "A user can only update comments they have created themselves. A `403 Forbidden` error is returned if they try to update another user's comment.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User is not the owner of the comment",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Comment not found or user not authorized to access it",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}")
  public SuccessResponseDTO<CommentResponseDTO> updateCommentById(
    @PathVariable String id,
    @RequestBody @Valid CommentUpdateDTO commentUpdateDTO,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Comment update successfully")
      .data(commentsService.updateCommentById(id, commentUpdateDTO, currentUser))
      .build();
  }

  @Operation(
    summary = "Delete a comment by ID",
    description = "Performs a soft delete on a specific comment. Access to the comment is first determined by the user's role, followed by an ownership check.\n\n" +
      "**Access Logic:**\n" +
      "*   `ADMIN`: Can access any comment.\n" +
      "*   `INSTRUCTOR`: Can access non-deleted comments on non-deleted exercises that are either public or created by them.\n" +
      "*   `STUDENT`: Can access non-deleted comments on non-deleted exercises belonging to a group they are a member of.\n\n" +
      "**Ownership Logic:**\n" +
      "A user can only delete comments they have created themselves. A `403 Forbidden` error is returned if they try to delete another user's comment.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Comment deleted successfully"),
      @ApiResponse(
        responseCode = "403",
        description = "User is not the owner of the comment",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Comment not found or user not authorized to access it",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  public SuccessResponseDTO<CommentResponseDTO> deleteCommentById(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(200)
      .message("Comment deleted successfully")
      .data(commentsService.deleteCommentById(id, currentUser))
      .build();
  }

  @Operation(
    summary = "Report a comment by ID",
    description = "Reports a comment. If the report count reaches a system-defined threshold, the comment is automatically soft-deleted. Access to the comment is determined by the user's role.\n\n" +
      "**Access Logic:**\n" +
      "*   `ADMIN`: Can access any comment.\n" +
      "*   `INSTRUCTOR`: Can access non-deleted comments on non-deleted exercises that are either public or created by them.\n" +
      "*   `STUDENT`: Can access non-deleted comments on non-deleted exercises belonging to a group they are a member of.",
    responses = {
      @ApiResponse(responseCode = "201", description = "Comment reported successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Comment not found or user not authorized to access it",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/report/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<CommentResponseDTO> reportCommentById(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CommentResponseDTO>builder()
      .status(201)
      .message("Comment report successfully")
      .data(commentsService.reportCommentById(id, 1, currentUser))
      .build();
  }
}
