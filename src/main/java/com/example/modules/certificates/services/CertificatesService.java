package com.example.modules.certificates.services;

import com.example.modules.auth.enums.Role;
import com.example.modules.certificates.dtos.CertificateIssueDTO;
import com.example.modules.certificates.dtos.CertificateResponseDTO;
import com.example.modules.certificates.dtos.CertificatesSearchDTO;
import com.example.modules.certificates.dtos.UserCertificateResponseDTO;
import com.example.modules.certificates.dtos.UserCertificatesSearchDTO;
import com.example.modules.certificates.entities.Certificate;
import com.example.modules.certificates.exceptions.CertificateAlreadyBeenIssuedException;
import com.example.modules.certificates.exceptions.CertificateNotFoundException;
import com.example.modules.certificates.repositories.CertificatesRepository;
import com.example.modules.certificates.utils.CertificateMapper;
import com.example.modules.certificates.utils.CertificatesSpecification;
import com.example.modules.courses.entities.Course;
import com.example.modules.courses.exceptions.CourseNotFoundException;
import com.example.modules.courses.repositories.CoursesRepository;
import com.example.modules.courses.utils.CoursesSpecification;
import com.example.modules.users.entities.User;
import com.example.modules.users.exceptions.UserNotFoundException;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UsersSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CertificatesService {

  CertificatesRepository certificatesRepository;
  CertificateMapper certificateMapper;
  UsersRepository usersRepository;
  CoursesRepository coursesRepository;

  public Page<CertificateResponseDTO> findAllCertificates(
    CertificatesSearchDTO certificatesSearchDTO
  ) {
    return certificatesRepository
      .findAll(
        CertificatesSpecification.builder()
          .withStudentIds(certificatesSearchDTO.getStudentId())
          .withCourseIds(certificatesSearchDTO.getCourseId())
          .notDeleted()
          .build(),
        certificatesSearchDTO.toPageRequest()
      )
      .map(certificateMapper::toCertificateResponseDTO);
  }

  public Page<UserCertificateResponseDTO> findAllCertificatesOfCurrentUser(
    UserCertificatesSearchDTO userCertificatesSearchDTO,
    User currentUser
  ) {
    return certificatesRepository
      .findAll(
        CertificatesSpecification.builder()
          .withStudentId(currentUser.getId())
          .withCourseIds(userCertificatesSearchDTO.getCourseId())
          .notDeleted()
          .build(),
        userCertificatesSearchDTO.toPageRequest()
      )
      .map(certificateMapper::toUserCertificateResponseDTO);
  }

  @Transactional
  public CertificateResponseDTO issueCertificate(CertificateIssueDTO certificateIssueDTO) {
    String studentId = certificateIssueDTO.getStudentId();
    String courseId = certificateIssueDTO.getCourseId();

    if (
      certificatesRepository.exists(
        CertificatesSpecification.builder()
          .withStudentId(studentId)
          .withCourseId(courseId)
          .notDeleted()
          .build()
      )
    ) {
      throw new CertificateAlreadyBeenIssuedException();
    }

    User user = usersRepository
      .findOne(
        UsersSpecification.builder()
          .withAccountRole(Role.STUDENT)
          .notDeleted()
          .withId(certificateIssueDTO.getStudentId())
          .build()
      )
      .orElseThrow(UserNotFoundException::new);

    Course course = coursesRepository
      .findOne(
        CoursesSpecification.builder()
          .notDeleted()
          .withId(certificateIssueDTO.getCourseId())
          .build()
      )
      .orElseThrow(CourseNotFoundException::new);

    return certificateMapper.toCertificateResponseDTO(
      certificatesRepository.save(
        Certificate.builder()
          .user(user)
          .course(course)
          .reason(certificateIssueDTO.getReason())
          .build()
      )
    );
  }

  @Transactional
  public void revokeCertificate(String certificateId) {
    Certificate certificate = certificatesRepository
      .findOne(CertificatesSpecification.builder().withId(certificateId).notDeleted().build())
      .orElseThrow(CertificateNotFoundException::new);

    certificate.softDelete();
    certificatesRepository.save(certificate);
  }
}
