package com.example.modules.certificates.listeners;

import com.example.modules.certificates.dtos.CourseUpdatedEventDTO;
import com.example.modules.certificates.entities.Certificate;
import com.example.modules.certificates.publishers.CourseUpdatedEventPublisher;
import com.example.modules.certificates.repositories.CertificatesRepository;
import com.example.modules.certificates.utils.CertificatesSpecification;
import com.example.modules.courses.dtos.CourseWithProgressDTO.Progress;
import com.example.modules.courses.entities.Course;
import com.example.modules.courses.repositories.CoursesRepository;
import com.example.modules.courses.services.CoursesService;
import com.example.modules.courses.utils.CoursesSpecification;
import com.example.modules.users.entities.User;
import io.lettuce.core.RedisBusyException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseUpdatedEventListener
  implements StreamListener<String, ObjectRecord<String, CourseUpdatedEventDTO>> {

  private static final String GROUP_NAME = "certificate-course-update-workers";

  private final RedisTemplate<String, Object> redisTemplate;
  private final CoursesRepository coursesRepository;
  private final CoursesService coursesService;
  private final CertificatesRepository certificatesRepository;

  @PostConstruct
  private void createConsumerGroup() {
    try {
      redisTemplate.opsForStream().createGroup(CourseUpdatedEventPublisher.STREAM_KEY, GROUP_NAME);
    } catch (RedisSystemException e) {
      if (e.getCause() instanceof RedisBusyException) {
        log.info(
          "Consumer group '{}' already exists for stream '{}'.",
          GROUP_NAME,
          CourseUpdatedEventPublisher.STREAM_KEY
        );
      } else {
        log.error("Error creating consumer group: {}", e.getMessage());
      }
    }
  }

  @Override
  public void onMessage(ObjectRecord<String, CourseUpdatedEventDTO> message) {
    CourseUpdatedEventDTO event = message.getValue();
    log.info("Received course updated event [Message ID: {}]", message);

    try {
      Course course = coursesRepository
        .findOne(
          CoursesSpecification.builder()
            .fetchEnrolledStudents()
            .withId(event.courseId())
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

      redisTemplate.opsForStream().acknowledge(GROUP_NAME, message);
      log.info("Successfully processed and acknowledged message '{}'", message.getId());
    } catch (Exception e) {
      log.error(
        "Failed to process message {}. It will be retired later. Error: {}",
        message.getId(),
        e.getMessage()
      );
    }
  }
}
