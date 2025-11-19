package com.example.modules.certificates.utils;

import com.example.modules.certificates.dtos.CertificateResponseDTO;
import com.example.modules.certificates.dtos.UserCertificateResponseDTO;
import com.example.modules.certificates.entities.Certificate;
import com.example.modules.courses.utils.CourseMapper;
import com.example.modules.users.utils.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = { UserMapper.class, CourseMapper.class })
@Slf4j
public abstract class CertificateMapper {

  @Named("toCertificateResponseDTO")
  @Mapping(target = "user", qualifiedByName = "toUserProfileDTO")
  @Mapping(target = "course", qualifiedByName = "toCourseResponseDTO")
  public abstract CertificateResponseDTO toCertificateResponseDTO(Certificate certificate);

  @Named("toUserCertificateResponseDTO")
  @Mapping(target = "course", qualifiedByName = "toCourseResponseDTO")
  public abstract UserCertificateResponseDTO toUserCertificateResponseDTO(Certificate certificate);
}
