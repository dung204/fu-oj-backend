package com.example.base.validators;

import com.example.base.annotations.File;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Set;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

public class FileValidator implements ConstraintValidator<File, MultipartFile> {

  private Set<String> allowedTypes;
  private long maxSizeInBytes;

  @Override
  public void initialize(File constraintAnnotation) {
    this.allowedTypes = Set.of(constraintAnnotation.allowedTypes());
    this.maxSizeInBytes = DataSize.of(
      constraintAnnotation.maxSize(),
      constraintAnnotation.sizeUnit()
    ).toBytes();
  }

  @Override
  public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
    if (file.getSize() > maxSizeInBytes) {
      context.disableDefaultConstraintViolation();
      context
        .buildConstraintViolationWithTemplate(
          "File size cannot exceed %s".formatted(formatDataSize(maxSizeInBytes))
        )
        .addConstraintViolation();
      return false;
    }

    String fileType = file.getContentType();
    if (!matchesContentType(fileType)) {
      context.disableDefaultConstraintViolation();
      context
        .buildConstraintViolationWithTemplate(
          "Only files of these types are allowed: %s".formatted(String.join(", ", allowedTypes))
        )
        .addConstraintViolation();
      return false;
    }

    return true;
  }

  private boolean matchesContentType(String actualType) {
    if (actualType == null || actualType.isEmpty()) return false;

    return allowedTypes
      .stream()
      .anyMatch(type -> {
        if (type.endsWith("/*")) {
          return actualType.split("\\/")[0].equals(type.split("\\/")[0]);
        }
        return actualType.equals(type);
      });
  }

  private String formatDataSize(long bytes) {
    DataSize dataSize = DataSize.ofBytes(bytes);

    if (bytes >= DataSize.ofTerabytes(1).toBytes()) {
      return dataSize.toTerabytes() + "TB";
    } else if (bytes >= DataSize.ofGigabytes(1).toBytes()) {
      return dataSize.toGigabytes() + "GB";
    } else if (bytes >= DataSize.ofMegabytes(1).toBytes()) {
      return dataSize.toMegabytes() + "MB";
    } else if (bytes >= DataSize.ofKilobytes(1).toBytes()) {
      return dataSize.toKilobytes() + "KB";
    }
    return bytes + "B";
  }
}
