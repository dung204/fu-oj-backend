package com.example.modules.certificates.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class CertificateAlreadyBeenIssuedException extends ResponseStatusException {

  public CertificateAlreadyBeenIssuedException() {
    super(HttpStatus.CONFLICT, "Certificate of this course has been issued for this user.");
  }
}
