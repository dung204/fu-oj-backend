package com.example.modules.courses.utils;

import com.example.modules.courses.dtos.CourseResponseDTO;
import com.example.modules.courses.dtos.CourseWithProgressDTO;
import com.example.modules.courses.entities.Course;
import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.minio.services.MinioService;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class CourseMapper {

  @Autowired
  protected MinioService minioService;

  @Named("toCourseResponseDTO")
  @Mapping(source = "image", target = "image", qualifiedByName = "mapImage")
  public abstract CourseResponseDTO toCourseResponseDTO(Course course);

  @Named("toCourseWithProgressDTO")
  @Mapping(source = "image", target = "image", qualifiedByName = "mapImage")
  @Mapping(target = "progress", ignore = true)
  public abstract CourseWithProgressDTO toCourseWithProgressDTO(Course course);

  @Named("mapImage")
  protected MinioFileResponse mapImage(String imageFileName) {
    if (imageFileName == null || imageFileName.trim().isEmpty()) {
      return null;
    }

    try {
      String presignedUrl = minioService.generatePresignedUrl(imageFileName);
      return MinioFileResponse.builder().fileName(imageFileName).url(presignedUrl).build();
    } catch (Exception e) {
      log.warn("Failed to generate presigned URL for avatar: {}", imageFileName, e);
      return null;
    }
  }
}
