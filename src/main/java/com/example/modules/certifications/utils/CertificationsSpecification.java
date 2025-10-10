package com.example.modules.certifications.utils;

import com.example.base.utils.SpecificationBuilder;
import com.example.modules.certifications.entities.Certification;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificationsSpecification extends SpecificationBuilder<Certification> {

  public static CertificationsSpecification builder() {
    return new CertificationsSpecification();
  }
}
