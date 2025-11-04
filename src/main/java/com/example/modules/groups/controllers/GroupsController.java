package com.example.modules.groups.controllers;

import static com.example.base.utils.AppRoutes.GROUPS_PREFIX;

import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.groups.dtos.*;
import com.example.modules.groups.services.GroupService;
import com.example.modules.users.dtos.StudentsSearchDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = GROUPS_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "groups", description = "Operations related to groups")
@RequiredArgsConstructor
public class GroupsController {

  private final GroupService groupsService;

  @Operation(
    summary = "Retrieve a list of groups based on the current authenticated user",
    description = "Fetches a paginated list of groups. The returned list depends on the role of the authenticated user:\n" +
      "  * `ADMIN`: Returns all groups in the system (public and private).\n" +
      "  * `INSTRUCTOR`: Returns all groups created by the instructor (public and private).\n" +
      "  * `STUDENT`: Behavior depends on the 'filter' parameter:\n" +
      "    * If `filter=joined`, returns all groups the student is a member of.\n" +
      "    * Otherwise, returns all public groups in the system.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Groups retrieved successfully"),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<GroupResponseDTO> getGroups(
    @ParameterObject @Valid GroupsSearchDTO groupsSearchDTO,
    @CurrentUser User user
  ) {
    return PaginatedSuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("Groups retrieved successfully")
      .page(groupsService.getGroups(user, groupsSearchDTO))
      .filters(groupsSearchDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Retrieve a single group by its ID",
    description = "Fetches a single group by its unique ID. Access is determined by the user's role:\n" +
      "  * `ADMIN`: Can retrieve any group.\n" +
      "  * `INSTRUCTOR`: Can retrieve any public group or any private group they own.\n" +
      "  * `STUDENT`: Can retrieve any public group or any private group they are a member of.\n\n" +
      "If a user is not authorized to view a group, a 404 Not Found error is returned to prevent information leakage.",
    responses = {
      @ApiResponse(responseCode = "200", description = "Group retrieved successfully"),
      @ApiResponse(
        responseCode = "404",
        description = "Group not found or user is not authorized to view it",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  public SuccessResponseDTO<GroupResponseDTO> getGroupById(
    @PathVariable String id,
    @CurrentUser User user
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("Group retrieved successfully")
      .data(groupsService.getGroupById(id, user))
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Create new group (for INSTRUCTOR only)",
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
    @RequestBody @Valid GroupRequestDTO groupRequestDTO,
    @CurrentUser User user
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Group created successfully")
      .data(groupsService.addGroup(user, groupRequestDTO))
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Update group by ID (for INSTRUCTOR only)",
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
  @PutMapping("/{id}")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupResponseDTO> updateGroup(
    @PathVariable String id,
    @RequestBody @Valid GroupUpdateRequestDTO groupUpdateRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Update group successfully")
      .data(groupsService.updateGroup(id, groupUpdateRequestDTO))
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Delete group (for INSTRUCTOR only)",
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
  public SuccessResponseDTO<GroupResponseDTO> deleteGroupById(@PathVariable String id) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("Group delete successfully")
      .data(groupsService.deleteGroup(id))
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Add exercises to a group (for INSTRUCTOR only)",
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
  @PostMapping("/{id}/exercises")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<GroupResponseDTO> addExerciseToGroup(
    @PathVariable String id,
    @Valid @RequestBody ListExerciseToGroupRequestDTO addExerciseToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Add exercise to group successfully")
      .data(groupsService.addExerciseToGroup(id, addExerciseToGroupRequestDTO.getExerciseIds()))
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
  @Operation(
    summary = "Remove exercises from a group (for INSTRUCTOR only)",
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
  @DeleteMapping("/{id}/exercises")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupResponseDTO> removeExercisesFromGroup(
    @PathVariable String id,
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

  @Operation(
    summary = "Get exercises by group ID",
    description = "Retrieve all exercises that belong to a specific group using the group ID.\n\n" +
      "The access to the group is the same as in the get group by ID endpoint.",
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
  @GetMapping("/{id}/exercises")
  @ResponseStatus(HttpStatus.OK)
  public PaginatedSuccessResponseDTO<ExerciseResponseDTO> getExerciseByGroupId(
    @PathVariable String id,
    @ParameterObject @Valid ExerciseQueryDTO exerciseQueryDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<ExerciseResponseDTO>builder()
      .status(200)
      .message("Get exercises by group ID successfully")
      .page(groupsService.getExercisesByGroupId(id, exerciseQueryDTO, currentUser))
      .filters(exerciseQueryDTO.getFilters())
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
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
  @PostMapping("/{id}/students")
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<GroupResponseDTO> addStudentsToGroup(
    @PathVariable String id,
    @Valid @RequestBody ListStudentToGroupRequestDTO addStudentToGroupRequestDTO
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(201)
      .message("Add students to group successfully")
      .data(groupsService.addStudentsToGroup(id, addStudentToGroupRequestDTO.getStudentIds()))
      .build();
  }

  @Operation(
    summary = "Get students by group ID",
    description = "Retrieve all students that belong to a specific group using the group ID.\n\n" +
      "The access to the group is the same as in the get group by ID endpoint.",
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
        description = "Group not found or user is not authorized to access the group",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}/students")
  @ResponseStatus(HttpStatus.OK)
  public PaginatedSuccessResponseDTO<UserProfileDTO> getStudentsByGroupId(
    @PathVariable String id,
    @ParameterObject @Valid StudentsSearchDTO studentsSearchDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<UserProfileDTO>builder()
      .status(200)
      .message("Get students by groupId successfully")
      .page(groupsService.getStudentsByGroupId(id, studentsSearchDTO, currentUser))
      .filters(studentsSearchDTO.getFilters())
      .build();
  }

  @AllowRoles(Role.INSTRUCTOR)
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
  @DeleteMapping("/{id}/students")
  @ResponseStatus(HttpStatus.OK)
  public SuccessResponseDTO<GroupResponseDTO> removeStudentsFromGroup(
    @PathVariable String id,
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

  @Operation(
    summary = "Join a group using a join code (for STUDENT only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Successfully joined the group"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid join code provided",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "403",
        description = "Only students can join groups",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "404",
        description = "Group with the provided code not found",
        content = @Content
      ),
      @ApiResponse(
        responseCode = "409",
        description = "Student is already a member of this group",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @AllowRoles({ Role.STUDENT, Role.INSTRUCTOR, Role.ADMIN })
  @PostMapping("/join")
  public SuccessResponseDTO<GroupResponseDTO> joinGroupByCode(
    @RequestBody @Valid JoinGroupRequestDTO joinGroupRequestDTO,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<GroupResponseDTO>builder()
      .status(200)
      .message("Joined group successfully!")
      .data(groupsService.joinGroupByCode(currentUser, joinGroupRequestDTO))
      .build();
  }
}
