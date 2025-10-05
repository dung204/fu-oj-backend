package com.example.modules.groups.controllers;

import static com.example.base.utils.AppRoutes.GROUPS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.services.GroupsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = GROUPS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "groups", description = "Operations related to groups")
@RequiredArgsConstructor
public class GroupsController {

  private final GroupsService groupsService;

  @Public
  @Operation(
    summary = "Create new group",
    responses = {
      @ApiResponse(responseCode = "201", description = "Create new group successfully"),
      @ApiResponse(responseCode = "400", description = "Group is error format", content = @Content),
      @ApiResponse(
        responseCode = "401",
        description = "Create new group is faild",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<GroupResponseDTO> createGroup(
    @RequestBody @Valid GroupRequestDTO groupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Group created successfully")
      .data(groupsService.addGroup(groupRequestDTO))
      .build();
  }
}
