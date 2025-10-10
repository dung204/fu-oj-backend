package com.example.modules.certifications.utils;

import com.example.modules.certifications.dtos.CertificationResponseDTO;
import com.example.modules.certifications.entities.Certification;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class CertificationMapper {

  public abstract CertificationResponseDTO toCertificationResponseDTO(Certification certification);
}
