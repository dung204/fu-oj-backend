package com.example.modules.certificates.dtos;

import java.io.Serializable;

public record SubmissionAcceptedEventDTO(String studentId, String exerciseId) implements
  Serializable {}
