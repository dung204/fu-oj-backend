package com.example.modules.certificates.repositories;

import com.example.modules.certificates.entities.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificatesRepository
  extends JpaRepository<Certificate, String>, JpaSpecificationExecutor<Certificate> {}
