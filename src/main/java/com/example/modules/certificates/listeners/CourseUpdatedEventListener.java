package com.example.modules.certificates.listeners;

import com.example.modules.certificates.dtos.CourseUpdatedEventDTO;
import com.example.modules.certificates.entities.Certificate;
import com.example.modules.certificates.repositories.CertificatesRepository;
import com.example.modules.certificates.utils.CertificatesSpecification;
import com.example.modules.courses.dtos.CourseWithProgressDTO.Progress;
import com.example.modules.courses.entities.Course;
import com.example.modules.courses.repositories.CoursesRepository;
import com.example.modules.courses.services.CoursesService;
import com.example.modules.courses.utils.CoursesSpecification;
import com.example.modules.redis.configs.listeners.RedisStreamListener;
import com.example.modules.users.entities.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CourseUpdatedEventListener extends RedisStreamListener<CourseUpdatedEventDTO> {

  private final CoursesRepository coursesRepository;
  private final CoursesService coursesService;
  private final CertificatesRepository certificatesRepository;

  public CourseUpdatedEventListener(
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    CoursesRepository coursesRepository,
    CoursesService coursesService,
    CertificatesRepository certificatesRepository
  ) {
    super(redisTemplate, objectMapper);
    this.coursesRepository = coursesRepository;
    this.coursesService = coursesService;
    this.certificatesRepository = certificatesRepository;
  }

  @Override
  public String getStreamKey() {
    return "certificates:events:course-updated";
  }

  @Override
  public String getConsumerGroup() {
    return "certificate-course-update-workers";
  }

  @Override
  public Class<CourseUpdatedEventDTO> getTargetType() {
    return CourseUpdatedEventDTO.class;
  }

  @Override
  protected void process(String messageId, CourseUpdatedEventDTO dto) {
    Course course = coursesRepository
      .findOne(
        CoursesSpecification.builder()
          .fetchEnrolledStudents()
          .withId(dto.getCourseId())
          .notDeleted()
          .build()
      )
      .get();

    for (User student : course.getEnrolledStudents()) {
      boolean hasCertExisted = certificatesRepository.exists(
        CertificatesSpecification.builder()
          .withCourseId(course.getId())
          .withStudentId(student.getId())
          .notDeleted()
          .build()
      );
      if (hasCertExisted) continue;

      Progress progress = coursesService.getCourseProgress(course, student);
      if (progress.getIsCompleted()) {
        Certificate certificate = Certificate.builder().course(course).user(student).build();
        certificatesRepository.save(certificate);
        log.info(
          "Successfully issued new certificate for student '{}' in course '{}'.",
          student.getId(),
          course.getTitle()
        );
      }
    }
  }
}
