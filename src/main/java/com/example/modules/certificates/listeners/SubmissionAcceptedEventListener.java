package com.example.modules.certificates.listeners;

import com.example.modules.certificates.dtos.SubmissionAcceptedEventDTO;
import com.example.modules.certificates.entities.Certificate;
import com.example.modules.certificates.publishers.SubmissionAcceptedEventPublisher;
import com.example.modules.certificates.repositories.CertificatesRepository;
import com.example.modules.certificates.utils.CertificatesSpecification;
import com.example.modules.courses.dtos.CourseWithProgressDTO.Progress;
import com.example.modules.courses.entities.Course;
import com.example.modules.courses.repositories.CoursesRepository;
import com.example.modules.courses.services.CoursesService;
import com.example.modules.courses.utils.CoursesSpecification;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UsersSpecification;
import jakarta.annotation.PostConstruct;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionAcceptedEventListener
  implements StreamListener<String, ObjectRecord<String, SubmissionAcceptedEventDTO>> {

  public static final String GROUP_NAME = "certificate-submission-workers";

  private final RedisTemplate<String, Object> redisTemplate;
  private final UsersRepository usersRepository;
  private final CoursesRepository coursesRepository;
  private final CoursesService coursesService;
  private final CertificatesRepository certificatesRepository;

  @PostConstruct
  private void createConsumerGroup() {
    try {
      redisTemplate
        .opsForStream()
        .createGroup(SubmissionAcceptedEventPublisher.STREAM_KEY, GROUP_NAME);
    } catch (Exception e) {
      if (e.getMessage().contains("BUSYGROUP")) {
        log.info(
          "Consumer group '{}' already exists for stream: '{}'.",
          GROUP_NAME,
          SubmissionAcceptedEventPublisher.STREAM_KEY
        );
      } else {
        log.error("Error creating consumer group: {}", e.getMessage());
      }
    }
  }

  @Override
  public void onMessage(ObjectRecord<String, SubmissionAcceptedEventDTO> message) {
    SubmissionAcceptedEventDTO event = message.getValue();
    log.info("Received submission accepted event [Message ID: {}]", message.getId());

    try {
      List<Course> courses = coursesRepository.findAll(
        CoursesSpecification.builder()
          .withExerciseId(event.exerciseId())
          .withEnrolledStudentId(event.studentId())
          .notDeleted()
          .build()
      );

      for (Course course : courses) {
        boolean hasCertExisted = certificatesRepository.exists(
          CertificatesSpecification.builder()
            .withCourseId(course.getId())
            .withStudentId(event.studentId())
            .notDeleted()
            .build()
        );
        if (hasCertExisted) {
          log.info(
            "Certificate for student '{}' in course '{}' already exists. Skipping.",
            event.studentId(),
            course.getId()
          );
          continue;
        }

        User user = usersRepository
          .findOne(UsersSpecification.builder().withId(event.studentId()).build())
          .get();
        Progress progress = coursesService.getCourseProgress(course, user);
        if (progress.getIsCompleted()) {
          Certificate certificate = Certificate.builder().course(course).user(user).build();
          certificatesRepository.save(certificate);
          log.info(
            "Successfully issued new certificate for student '{}' in course '{}'.",
            user.getId(),
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
