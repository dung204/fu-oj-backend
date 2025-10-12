package com.example.modules.users.utils;

import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.minio.services.MinioService;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class UserMapper {

  @Autowired
  protected MinioService minioService;

  @Named("toUserProfileDTO")
  @Mapping(source = "account.email", target = "email")
  @Mapping(source = "account.role", target = "role")
  @Mapping(source = "avatar", target = "avatar", qualifiedByName = "mapAvatar")
  public abstract UserProfileDTO toUserProfileDTO(User user);

  @Named("mapAvatar")
  protected MinioFileResponse mapAvatar(String avatarFileName) {
    if (avatarFileName == null || avatarFileName.trim().isEmpty()) {
      return null;
    }

    try {
      String presignedUrl = minioService.generatePresignedUrl(avatarFileName);
      return MinioFileResponse.builder().fileName(avatarFileName).url(presignedUrl).build();
    } catch (Exception e) {
      log.warn("Failed to generate presigned URL for avatar: {}", avatarFileName, e);
      return null;
    }
  }
}
