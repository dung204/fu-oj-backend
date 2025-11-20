package com.example.modules.courses.services;

import com.example.base.utils.ObjectUtils;
import com.example.modules.auth.enums.Role;
import com.example.modules.certificates.dtos.CourseUpdatedEventDTO;
import com.example.modules.certificates.publishers.CourseUpdatedEventPublisher;
import com.example.modules.courses.dtos.CourseCreateDTO;
import com.example.modules.courses.dtos.CourseExerciseRequestDTO;
import com.example.modules.courses.dtos.CourseResponseDTO;
import com.example.modules.courses.dtos.CourseUpdateDTO;
import com.example.modules.courses.dtos.CourseWithProgressDTO;
import com.example.modules.courses.dtos.CourseWithProgressDTO.Progress;
import com.example.modules.courses.dtos.CoursesSearchDTO;
import com.example.modules.courses.entities.Course;
import com.example.modules.courses.exceptions.AlreadyEnrolledInCourseException;
import com.example.modules.courses.exceptions.CourseNotFoundException;
import com.example.modules.courses.repositories.CoursesRepository;
import com.example.modules.courses.utils.CourseMapper;
import com.example.modules.courses.utils.CoursesSpecification;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.exceptions.ExerciseNotFoundException;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.exercises.utils.ExercisesSpecification;
import com.example.modules.redis.publishers.RedisStreamPublisher;
import com.example.modules.submissions.repositories.SubmissionsRepository;
import com.example.modules.users.entities.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CoursesService {

  CoursesRepository coursesRepository;
  CourseMapper courseMapper;
  SubmissionsRepository submissionsRepository;
  ExercisesRepository exercisesRepository;
  CourseUpdatedEventPublisher courseUpdatedEventPublisher;
  RedisStreamPublisher redisStreamPublisher;

  @Transactional
  public CourseResponseDTO createCourse(CourseCreateDTO courseCreateDTO) {
    Course course = Course.builder()
      .title(courseCreateDTO.getTitle())
      .description(courseCreateDTO.getDescription())
      .build();

    return courseMapper.toCourseResponseDTO(coursesRepository.save(course));
  }

  @Transactional
  public CourseResponseDTO updateCourse(String courseId, CourseUpdateDTO courseUpdateDTO) {
    Course course = coursesRepository
      .findOne(CoursesSpecification.builder().withId(courseId).notDeleted().build())
      .orElseThrow(CourseNotFoundException::new);

    ObjectUtils.assign(course, courseUpdateDTO);

    return courseMapper.toCourseResponseDTO(coursesRepository.save(course));
  }

  @Transactional
  public void deleteCourse(String courseId) {
    Course course = coursesRepository
      .findOne(CoursesSpecification.builder().withId(courseId).notDeleted().build())
      .orElseThrow(CourseNotFoundException::new);

    course.softDelete();
    coursesRepository.save(course);
  }

  public Page<CourseResponseDTO> findAllCourses(CoursesSearchDTO coursesSearchDTO) {
    return coursesRepository
      .findAll(
        CoursesSpecification.builder().containsTitle(coursesSearchDTO.getTitle()).build(),
        coursesSearchDTO.toPageRequest()
      )
      .map(courseMapper::toCourseResponseDTO);
  }

  public CourseWithProgressDTO getCourseDetailsAndProgressByCourseId(String id, User currentUser) {
    Course course = coursesRepository
      .findOne(CoursesSpecification.builder().fetchExercises().withId(id).notDeleted().build())
      .orElseThrow(CourseNotFoundException::new);

    CourseWithProgressDTO response = courseMapper.toCourseWithProgressDTO(course);

    if (currentUser.getAccount().getRole() == Role.STUDENT) {
      response.setProgress(getCourseProgress(course, currentUser));
    }

    return response;
  }

  @Transactional
  public void addExercisesToCourse(String courseId, CourseExerciseRequestDTO requestDTO) {
    Course course = coursesRepository
      .findOne(CoursesSpecification.builder().withId(courseId).notDeleted().build())
      .orElseThrow(CourseNotFoundException::new);

    Set<String> inputIds = requestDTO.getExerciseIds();
    List<Exercise> exercises = exercisesRepository.findAll(
      ExercisesSpecification.builder()
        .publicOnly()
        .onlyLatestVersion()
        .withIds(requestDTO.getExerciseIds())
        .build()
    );

    inputIds.removeAll(exercises.stream().map(Exercise::getId).collect(Collectors.toSet()));

    if (!inputIds.isEmpty()) {
      throw new ExerciseNotFoundException(
        "The following exercises are not found: " + inputIds.toString()
      );
    }

    course.getExercises().addAll(exercises);
    coursesRepository.save(course);
  }

  public void removeExercisesFromCourse(String courseId, CourseExerciseRequestDTO requestDTO) {
    Course course = coursesRepository
      .findOne(CoursesSpecification.builder().withId(courseId).notDeleted().build())
      .orElseThrow(CourseNotFoundException::new);

    Set<String> inputIds = requestDTO.getExerciseIds();
    List<Exercise> exercises = exercisesRepository.findAll(
      ExercisesSpecification.builder()
        .withCourseId(course.getId())
        .withIds(requestDTO.getExerciseIds())
        .build()
    );

    inputIds.removeAll(exercises.stream().map(Exercise::getId).collect(Collectors.toSet()));

    if (!inputIds.isEmpty()) {
      throw new ExerciseNotFoundException(
        "The following exercises are not found: " + inputIds.toString()
      );
    }

    course.getExercises().removeAll(exercises);
    coursesRepository.save(course);
    redisStreamPublisher.send(
      "certificates:events:course-updated",
      new CourseUpdatedEventDTO(courseId)
    );
  }

  @Transactional
  public CourseResponseDTO enrollInCourse(String id, User currentUser) {
    Course course = coursesRepository
      .findOne(
        CoursesSpecification.builder().fetchEnrolledStudents().withId(id).notDeleted().build()
      )
      .orElseThrow(CourseNotFoundException::new);

    if (course.getEnrolledStudents().contains(currentUser)) {
      throw new AlreadyEnrolledInCourseException();
    }

    course.getEnrolledStudents().add(currentUser);
    coursesRepository.save(course);

    CourseWithProgressDTO response = courseMapper.toCourseWithProgressDTO(course);
    response.setProgress(getCourseProgress(course, currentUser));

    return response;
  }

  public Progress getCourseProgress(Course course, User user) {
    long solvedCount = submissionsRepository.countDistinctAcceptedExercisesForUser(
      user.getId(),
      course.getExercises().stream().map(Exercise::getId).toList()
    );
    long totalCount = course.getExercises().size();
    double percentage = ((double) solvedCount / totalCount) * 100;
    boolean isCompleted = percentage == 100D;

    return Progress.builder()
      .solvedCount(solvedCount)
      .totalCount(totalCount)
      .percentage(percentage)
      .isCompleted(isCompleted)
      .build();
  }
}
