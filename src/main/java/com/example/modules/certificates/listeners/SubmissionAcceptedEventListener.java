package com.example.modules.certificates.listeners;

import com.example.modules.certificates.dtos.SubmissionAcceptedEventDTO;
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
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UsersSpecification;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SubmissionAcceptedEventListener
  extends RedisStreamListener<SubmissionAcceptedEventDTO> {

  private final UsersRepository usersRepository;
  private final CoursesRepository coursesRepository;
  private final CoursesService coursesService;
  private final CertificatesRepository certificatesRepository;

  public SubmissionAcceptedEventListener(
    StringRedisTemplate redisTemplate,
    ObjectMapper objectMapper,
    UsersRepository usersRepository,
    CoursesRepository coursesRepository,
    CoursesService coursesService,
    CertificatesRepository certificatesRepository
  ) {
    super(redisTemplate, objectMapper);
    this.usersRepository = usersRepository;
    this.coursesRepository = coursesRepository;
    this.coursesService = coursesService;
    this.certificatesRepository = certificatesRepository;
  }

  @Override
  public String getStreamKey() {
    return "certificates:events:submission-accepted";
  }

  @Override
  public String getConsumerGroup() {
    return "certificate-submission-workers";
  }

  @Override
  public Class<SubmissionAcceptedEventDTO> getTargetType() {
    return SubmissionAcceptedEventDTO.class;
  }

  @Override
  protected void process(String messageId, SubmissionAcceptedEventDTO dto) {
    List<Course> courses = coursesRepository.findAll(
      CoursesSpecification.builder()
        .fetchExercises()
        .withExerciseId(dto.getExerciseId())
        .withEnrolledStudentId(dto.getStudentId())
        .notDeleted()
        .build()
    );

    for (Course course : courses) {
      boolean hasCertExisted = certificatesRepository.exists(
        CertificatesSpecification.builder()
          .withCourseId(course.getId())
          .withStudentId(dto.getStudentId())
          .notDeleted()
          .build()
      );
      if (hasCertExisted) {
        log.info(
          "Certificate for student '{}' in course '{}' already exists. Skipping.",
          dto.getStudentId(),
          course.getId()
        );
        continue;
      }

      User user = usersRepository
        .findOne(UsersSpecification.builder().withId(dto.getStudentId()).build())
        .get();
      Progress progress = coursesService.getCourseProgress(course, user);
      if (progress.getIsCompleted()) {
        Certificate certificate = Certificate.builder()
          .course(course)
          .user(user)
          .name("Certificate of Completion - " + course.getTitle())
          .condition("Completed all exercises in the course")
          .build();
        certificatesRepository.save(certificate);
        log.info(
          "Successfully issued new certificate for student '{}' in course '{}'.",
          user.getId(),
          course.getTitle()
        );
      }
    }
  }
}
