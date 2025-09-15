package com.example.modules.posts.controllers;

import static com.example.base.utils.AppRoutes.POSTS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.annotations.OptionalAuth;
import com.example.modules.auth.annotations.Public;
import com.example.modules.posts.dtos.CreatePostDTO;
import com.example.modules.posts.dtos.PostResponseDTO;
import com.example.modules.posts.dtos.PostsSearchDTO;
import com.example.modules.posts.dtos.UpdatePostDTO;
import com.example.modules.posts.services.PostsService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = POSTS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "posts", description = "Operations related to posts")
public class PostsController {

  private final PostsService postsService;

  @Operation(
    summary = "Retrieve all existing public posts",
    responses = {
      @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @Public
  @GetMapping
  public PaginatedSuccessResponseDTO<PostResponseDTO> getAllPosts(
    @ParameterObject @Valid PostsSearchDTO postsSearchDTO
  ) {
    return PaginatedSuccessResponseDTO.<PostResponseDTO>builder()
      .message("Posts retrieved successfully.")
      .page(postsService.findAllPublicPosts(postsSearchDTO))
      .filters(postsSearchDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Get a post by ID",
    description = "Returns an existing, public post by ID. If current authenticated user is available, he can find his existing, private posts.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Posts retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @OptionalAuth
  @GetMapping("/{id}")
  public SuccessResponseDTO<PostResponseDTO> getPostById(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<PostResponseDTO>builder()
      .message("Post created successfully.")
      .data(postsService.findPostById(id, currentUser))
      .build();
  }

  @Operation(
    summary = "Create a new post",
    responses = {
      @ApiResponse(responseCode = "201", description = "Post created successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<PostResponseDTO> createPost(
    @RequestBody @Valid CreatePostDTO createPostDTO,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<PostResponseDTO>builder()
      .status(201)
      .message("Post created successfully.")
      .data(postsService.createPost(createPostDTO, currentUser))
      .build();
  }

  @Operation(
    summary = "Update an existing post",
    responses = {
      @ApiResponse(responseCode = "200", description = "Post is updated successfully"),
      @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}")
  public SuccessResponseDTO<PostResponseDTO> updatePost(
    @PathVariable String id,
    @RequestBody @Valid UpdatePostDTO updatePostDTO,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<PostResponseDTO>builder()
      .message("Post updated successfully.")
      .data(postsService.updatePost(id, updatePostDTO, currentUser))
      .build();
  }

  @Operation(
    summary = "Delete an existing post",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Post is deleted successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePost(@PathVariable String id, @CurrentUser User currentUser) {
    postsService.deletePost(id, currentUser);
  }

  @Operation(
    summary = "Restore a deleted post",
    responses = {
      @ApiResponse(responseCode = "200", description = "Post is restored successfully"),
      @ApiResponse(responseCode = "404", description = "Post not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/restore/{id}")
  public SuccessResponseDTO<PostResponseDTO> restorePost(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<PostResponseDTO>builder()
      .message("Post restored successfully.")
      .data(postsService.restorePost(id, currentUser))
      .build();
  }
}
