package com.example.modules.certifications.repositories;

import com.example.modules.certifications.entities.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationsRepository
  extends JpaRepository<Certification, String>, JpaSpecificationExecutor<Certification> {}
