package com.example.modules.certificates.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CertificateNotFoundException extends ResponseStatusException {

  public CertificateNotFoundException() {
    super(HttpStatus.NOT_FOUND, "Certificate not found.");
  }
}
