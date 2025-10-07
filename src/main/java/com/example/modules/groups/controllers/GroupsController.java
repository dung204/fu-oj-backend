package com.example.modules.groups.controllers;

import static com.example.base.utils.AppRoutes.GROUPS_PREFIX;

import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.Public;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.groups.dtos.*;
import com.example.modules.groups.services.GroupsService;
import com.example.modules.users.entities.User;
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
    @Valid @RequestBody ListExerciseToGroupRequestDTO addExerciseToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Add exercise to group successfully")
      .data(groupsService.addExerciseToGroup(id, addExerciseToGroupRequestDTO.getExerciseIds()))
      .build();
  }

  @Public
  @Operation(
    summary = "Remove exercises from a group",
    description = "Remove one or multiple exercises from a specific group by providing the group ID and a list of exercise IDs to remove.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercises removed from group successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid group ID format or invalid exercise IDs",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Unauthorized to modify this group",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Group or one of the exercises not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<GroupResponseDTO> removeExercisesFromGroup(
    @PathVariable @NotNull(message = "ID must be not null") String id,
    @Valid @RequestBody ListExerciseToGroupRequestDTO removeExerciseToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("remove exercise to group successfully")
      .data(
        groupsService.removeExercisesFromGroup(id, removeExerciseToGroupRequestDTO.getExerciseIds())
      )
      .build();
  }

  @Public
  @Operation(
    summary = "Get exercises by group ID",
    description = "Retrieve all exercises that belong to a specific group using the group ID.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved exercises of the group"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid group ID format",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Unauthorized access to this resource",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Group not found or no exercises available",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<Exercise>> getExerciseByGroupId(
    @PathVariable @NotNull(message = "Group ID cannot be null") String id
  ) {
    return SuccessResponseDTO.<List<Exercise>>builder()
      .status(200)
      .message("Get exercise by id successfully")
      .data(groupsService.getExerciseByGroupId(id))
      .build();
  }

  @Public
  @Operation(
    summary = "Add students to a group",
    description = "Add one or more students to the specified group by group ID.",
    responses = {
      @ApiResponse(responseCode = "201", description = "Students added to group successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid group ID or malformed request body",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Unauthorized to add students to this group",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Group not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<GroupResponseDTO> addStudentsToGroup(
    @PathVariable @NotNull(message = "ID must be not null") String id,
    @Valid @RequestBody ListStudentToGroupRequestDTO addStudentToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Add students to group successfully")
      .data(groupsService.addStudentsToGroup(id, addStudentToGroupRequestDTO.getStudentIds()))
      .build();
  }

  @Public
  @Operation(
    summary = "Get students by group ID",
    description = "Retrieve all students that belong to a specific group using the group ID.",
    responses = {
      @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved students of the group"
      ),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid group ID format",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Unauthorized access to this resource",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Group not found or no students available",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<List<User>> getStudentsByGroupId(
    @PathVariable @NotNull(message = "Group ID cannot be null") String id
  ) {
    return SuccessResponseDTO.<List<User>>builder()
      .status(200)
      .message("Get students by groupId successfully")
      .data(groupsService.getStudentsByGroupId(id))
      .build();
  }

  @Public
  @Operation(
    summary = "Remove students from a group",
    description = "Remove one or multiple students from a specific group by providing the group ID and a list of student IDs to remove.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Students removed from group successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid group ID format or invalid student IDs",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "401",
        description = "Unauthorized to modify this group",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Group or one of the students not found",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupResponseDTO> removeStudentsFromGroup(
    @PathVariable @NotNull(message = "ID must be not null") String id,
    @Valid @RequestBody ListStudentToGroupRequestDTO removeStudentToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("Remove students from group successfully")
      .data(
        groupsService.removeStudentsFromGroup(id, removeStudentToGroupRequestDTO.getStudentIds())
      )
      .build();
  }
}
