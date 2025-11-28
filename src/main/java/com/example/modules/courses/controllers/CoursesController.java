package com.example.modules.courses.controllers;

import static com.example.base.utils.AppRoutes.COURSES_PREFIX;

import com.example.base.annotations.File;
import com.example.base.dtos.PaginatedSuccessResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.annotations.AllowRoles;
import com.example.modules.auth.annotations.CurrentUser;
import com.example.modules.auth.enums.Role;
import com.example.modules.courses.dtos.CourseCreateDTO;
import com.example.modules.courses.dtos.CourseExerciseRequestDTO;
import com.example.modules.courses.dtos.CourseResponseDTO;
import com.example.modules.courses.dtos.CourseUpdateDTO;
import com.example.modules.courses.dtos.CourseWithProgressDTO;
import com.example.modules.courses.dtos.CoursesSearchDTO;
import com.example.modules.courses.services.CoursesService;
import com.example.modules.exercises.dtos.ExerciseQueryDTO;
import com.example.modules.exercises.dtos.ExerciseResponseDTO;
import com.example.modules.exercises.services.ExercisesService;
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
import org.springframework.util.unit.DataUnit;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(path = COURSES_PREFIX, produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "courses", description = "Operations related to courses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CoursesController {

  CoursesService coursesService;
  ExercisesService exercisesService;

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Create a new course (for ADMIN only)",
    responses = {
      @ApiResponse(responseCode = "201", description = "Course created successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SuccessResponseDTO<CourseResponseDTO> createCourse(
    @RequestBody @Valid CourseCreateDTO courseCreateDTO
  ) {
    return SuccessResponseDTO.<CourseResponseDTO>builder()
      .status(201)
      .message("Course created successfully.")
      .data(coursesService.createCourse(courseCreateDTO))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Update an existing course (for ADMIN only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Course updated successfully"),
      @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping("/{id}")
  public SuccessResponseDTO<CourseResponseDTO> updateCourse(
    @PathVariable String id,
    @RequestBody @Valid CourseUpdateDTO courseUpdateDTO
  ) {
    return SuccessResponseDTO.<CourseResponseDTO>builder()
      .status(201)
      .message("Course created successfully.")
      .data(coursesService.updateCourse(id, courseUpdateDTO))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Deleted an existing course (for ADMIN only)",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Course deleted successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCourse(@PathVariable String id) {
    coursesService.deleteCourse(id);
  }

  @Operation(
    summary = "Retrieve all existing courses",
    responses = {
      @ApiResponse(responseCode = "200", description = "Courses retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping
  public PaginatedSuccessResponseDTO<CourseResponseDTO> getAllCourses(
    @ParameterObject @Valid CoursesSearchDTO coursesSearchDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<CourseResponseDTO>builder()
      .message("Courses retrieved successfully.")
      .page(coursesService.findAllCourses(coursesSearchDTO, currentUser))
      .filters(coursesSearchDTO.getFilters())
      .build();
  }

  @Operation(
    summary = "Retrieve a course by ID and the progress of the current authenticated user in this course",
    description = "For `ADMIN` & `INSTRUCTOR`, the `progress` will always be `null`\n\n" +
      "For `STUDENT`, the `progress = null` means that the student hasn't enrolled in the course",
    responses = {
      @ApiResponse(responseCode = "200", description = "Course retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}")
  public SuccessResponseDTO<CourseWithProgressDTO> getCourseDetailsAndProgressByCourseId(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CourseWithProgressDTO>builder()
      .message("Course retrieved successfully.")
      .data(coursesService.getCourseDetailsAndProgressByCourseId(id, currentUser))
      .build();
  }

  @Operation(
    summary = "Retrieve all exercises of a course",
    responses = {
      @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @GetMapping("/{id}/exercises")
  public PaginatedSuccessResponseDTO<ExerciseResponseDTO> getExercisesByCourseId(
    @PathVariable String id,
    @ParameterObject @Valid ExerciseQueryDTO exerciseQueryDTO,
    @CurrentUser User currentUser
  ) {
    return PaginatedSuccessResponseDTO.<ExerciseResponseDTO>builder()
      .message("Exercise retrieved successfully.")
      .page(exercisesService.getExercisesByCourseId(id, exerciseQueryDTO, currentUser))
      .filters(exerciseQueryDTO.getFilters())
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Add exercises to a course (for ADMIN only)",
    description = "Only exercises with `visibility` of `PUBLIC` are allowed",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Exercises added successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(
        responseCode = "404",
        description = "Course not found, or one or more exercises not found/not public",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{id}/exercises")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void addExercisesToCourse(
    @PathVariable String id,
    @RequestBody @Valid CourseExerciseRequestDTO courseExerciseRequestDTO
  ) {
    coursesService.addExercisesToCourse(id, courseExerciseRequestDTO);
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Remove exercises from a course (for ADMIN only)",
    responses = {
      @ApiResponse(
        responseCode = "204",
        description = "Exercises removed successfully",
        content = @Content
      ),
      @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not an ADMIN", content = @Content),
      @ApiResponse(
        responseCode = "404",
        description = "Course not found, or one or more exercises not found in the course",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @DeleteMapping("/{id}/exercises")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void removeExercisesFromCourse(
    @PathVariable String id,
    @RequestBody @Valid CourseExerciseRequestDTO courseExerciseRequestDTO
  ) {
    coursesService.removeExercisesFromCourse(id, courseExerciseRequestDTO);
  }

  @AllowRoles({ Role.STUDENT, Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Enroll the current authenticated user in a course (for STUDENT only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Enrolled in course successfully"),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not a STUDENT", content = @Content),
      @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
      @ApiResponse(
        responseCode = "409",
        description = "User is already enrolled in this course",
        content = @Content
      ),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PostMapping("/{id}/enroll")
  public SuccessResponseDTO<CourseResponseDTO> createCourse(
    @PathVariable String id,
    @CurrentUser User currentUser
  ) {
    return SuccessResponseDTO.<CourseResponseDTO>builder()
      .message("Enrolled in course successfully.")
      .data(coursesService.enrollInCourse(id, currentUser))
      .build();
  }

  @AllowRoles({ Role.ADMIN, Role.INSTRUCTOR })
  @Operation(
    summary = "Update course image (for ADMIN & INSTRUCTOR only)",
    responses = {
      @ApiResponse(responseCode = "200", description = "Course image updated successfully"),
      @ApiResponse(
        responseCode = "400",
        description = "Invalid file (size > 1MB or not an image) or request",
        content = @Content
      ),
      @ApiResponse(responseCode = "401", description = "User is not logged in", content = @Content),
      @ApiResponse(
        responseCode = "403",
        description = "User is not an ADMIN or INSTRUCTOR",
        content = @Content
      ),
      @ApiResponse(responseCode = "404", description = "Course not found", content = @Content),
      @ApiResponse(responseCode = "500", description = "Internal Server Error", content = @Content),
    }
  )
  @PatchMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public SuccessResponseDTO<CourseResponseDTO> updateCourseImage(
    @PathVariable String id,
    @RequestPart("file") @Valid @File(
      maxSize = 1,
      sizeUnit = DataUnit.MEGABYTES,
      allowedTypes = "image/*"
    ) MultipartFile file
  ) throws Exception {
    return SuccessResponseDTO.<CourseResponseDTO>builder()
      .message("Course image updated successfully")
      .data(coursesService.updateCourseImage(id, file))
      .build();
  }
}
