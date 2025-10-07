package com.example.modules.groups.controllers;

import static com.example.base.utils.AppRoutes.GROUPS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import com.example.modules.groups.dtos.AddExerciseToGroupRequestDTO;
import com.example.modules.groups.dtos.GroupRequestDTO;
import com.example.modules.groups.dtos.GroupResponseDTO;
import com.example.modules.groups.dtos.GroupUpdateRequestDTO;
import com.example.modules.groups.services.GroupsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
        description = "Create new group is fail",
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

  @Public
  @Operation(
    summary = "Update group by id",
    responses = {
      @ApiResponse(responseCode = "200", description = "Update group by id successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Update group by id error",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PutMapping
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupResponseDTO> updateGroup(
    @RequestBody @Valid GroupUpdateRequestDTO groupUpdateRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("update group successfully")
      .data(groupsService.updateGroup(groupUpdateRequestDTO))
      .build();
  }

  @Public
  @Operation(
    summary = "Get group by ownerId",
    responses = {
      @ApiResponse(responseCode = "200", description = "Get group by ownerId successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Get group by ownerId error",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{ownerId}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<GroupResponseDTO>> getGroupByInstructorId(
    @PathVariable @NotNull(message = "User ID cannot be null") String ownerId
  ) {
    return SuccessResponseDTO.<List<GroupResponseDTO>>builder()
      .status(200)
      .message("Get group by ownerId successfully")
      .data(groupsService.getGroupByInstructorId(ownerId))
      .build();
  }

  @Public
  @Operation(
    summary = "Get all group",
    responses = {
      @ApiResponse(responseCode = "200", description = "Get all group successfully"),
      @ApiResponse(responseCode = "400", description = "Get all group error", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<GroupResponseDTO>> getGroups() {
    return SuccessResponseDTO.<List<GroupResponseDTO>>builder()
      .status(200)
      .message("Get group by ownerId successfully")
      .data(groupsService.getGroups())
      .build();
  }

  @Public
  @Operation(
    summary = "Delete group",
    responses = {
      @ApiResponse(responseCode = "201", description = "Delete group successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Group is error format id",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "Delete group is fail", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupResponseDTO> deleteGroupById(
    @PathVariable @NotNull(message = "ID must be not null") String id
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("Group delete successfully")
      .data(groupsService.deleteGroup(id))
      .build();
  }

  @Public
  @Operation(
    summary = "Add exercises to a group",
    description = "Add one or more exercises to the specified group by group ID.",
    responses = {
      @ApiResponse(responseCode = "201", description = "Exercises added to group successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid group ID or malformed request body",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Unauthorized to add exercises to this group",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Group not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<GroupResponseDTO> addExerciseToGroup(
    @PathVariable @NotNull(message = "ID must be not null") String id,
    @Valid @RequestBody AddExerciseToGroupRequestDTO addExerciseToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Add exercise to group successfully")
      .data(groupsService.addExerciseToGroup(id, addExerciseToGroupRequestDTO.getExerciseIds()))
      .build();
  }
}
