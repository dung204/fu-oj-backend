package com.example.modules.topics.controllers;

import static com.example.base.utils.AppRoutes.TOPICS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import com.example.modules.topics.dtos.CreateTopicDTO;
import com.example.modules.topics.dtos.TopicResponseDTO;
import com.example.modules.topics.dtos.TopicsSearchDTO;
import com.example.modules.topics.dtos.UpdateTopicDTO;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.services.TopicsService;
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
@RequestMapping(path = TOPICS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "topics", description = "Operations related to topics")
@RequiredArgsConstructor
public class TopicsController {

  private final TopicsService topicsService;

  @Operation(
    summary = "Retrieve all existing topics",
    responses = {
      @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @Public
  @GetMapping
  public PaginatedSuccessResponseDTO<TopicResponseDTO> getAllTopics(
    @ParameterObject @Valid TopicsSearchDTO topicsSearchDTO
  ) {
    return PaginatedSuccessResponseDTO.<TopicResponseDTO>builder()
      .message("Topics retrieved successfully.")
      .page(topicsService.findAllTopics(topicsSearchDTO))
      .filters(topicsSearchDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Get a topic by ID",
    responses = {
      @ApiResponse(responseCode = "200", description = "Topics retrieved successfully"),
      @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @Public
  @GetMapping("/{id}")
  public SuccessResponseDTO<TopicResponseDTO> getTopicById(@PathVariable String id) {
    return SuccessResponseDTO.<TopicResponseDTO>builder()
      .message("Topic created successfully.")
      .data(topicsService.findTopicById(id))
      .build();
  }

  // TODO: Add @AllowRoles(Roles.ADMIN)
  @Operation(
    summary = "Create a new Topic",
    responses = {
      @ApiResponse(responseCode = "201", description = "Topic created successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<TopicResponseDTO> createTopic(
    @RequestBody @Valid CreateTopicDTO createTopicDTO
  ) {
    return SuccessResponseDTO.<TopicResponseDTO>builder()
      .status(201)
      .message("Topic created successfully.")
      .data(topicsService.createTopic(createTopicDTO))
      .build();
  }

  // TODO: Add @AllowRoles(Roles.ADMIN)
  @Operation(
    summary = "Update an existing Topic",
    responses = {
      @ApiResponse(responseCode = "200", description = "Topic is updated successfully"),
      @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}")
  public SuccessResponseDTO<TopicResponseDTO> updateTopic(
    @PathVariable String id,
    @RequestBody @Valid UpdateTopicDTO updateTopicDTO
  ) {
    return SuccessResponseDTO.<TopicResponseDTO>builder()
      .message("Topic updated successfully.")
      .data(topicsService.updateTopic(id, updateTopicDTO))
      .build();
  }

  // TODO: Add @AllowRoles(Roles.ADMIN)
  @Operation(
    summary = "Delete an existing topic",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Topic is deleted successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteTopic(@PathVariable String id) {
    topicsService.deleteTopic(id);
  }

  // TODO: Add @AllowRoles(Roles.ADMIN)
  @Operation(
    summary = "Restore a deleted topic",
    responses = {
      @ApiResponse(responseCode = "200", description = "Topic is restored successfully"),
      @ApiResponse(responseCode = "404", description = "Topic not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/restore/{id}")
  public SuccessResponseDTO<TopicResponseDTO> restoreTopic(@PathVariable String id) {
    return SuccessResponseDTO.<TopicResponseDTO>builder()
      .message("Topic restored successfully.")
      .data(topicsService.restoreTopic(id))
      .build();
  }
}
