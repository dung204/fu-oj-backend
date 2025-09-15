package com.example.modules.users.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.auth.entities.Account;
import com.example.modules.auth.enums.Role;
import com.example.modules.minio.services.MinioService;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class UserMapperTest extends BaseServiceTest {

  private MinioService minioService;
  private UserMapper userMapper;

  @BeforeEach
  void setup() {
    minioService = mock(MinioService.class);
    userMapper = Mappers.getMapper(UserMapper.class);
    userMapper.minioService = minioService;
  }

  @Test
  void toUserProfileDTO_ShouldMapAvatar_WhenAvatarIsValid() throws Exception {
    String avatarFileName = "avatar.png";
    String presignedUrl = "http://minio.local/avatar.png";
    when(minioService.generatePresignedUrl(avatarFileName)).thenReturn(presignedUrl);

    User user = User.builder()
      .avatar(avatarFileName)
      .account(Account.builder().email("test@example.com").role(Role.USER).build())
      .build();

    UserProfileDTO dto = userMapper.toUserProfileDTO(user);

    assertNotNull(dto.getAvatar());
    assertEquals(avatarFileName, dto.getAvatar().getFileName());
    assertEquals(presignedUrl, dto.getAvatar().getUrl());
  }

  @Test
  void toUserProfileDTO_ShouldReturnNullAvatar_WhenAvatarIsNull() {
    User user = User.builder()
      .avatar(null)
      .account(Account.builder().email("test@example.com").role(Role.USER).build())
      .build();

    UserProfileDTO dto = userMapper.toUserProfileDTO(user);

    assertNull(dto.getAvatar());
  }

  @Test
  void toUserProfileDTO_ShouldReturnNullAvatar_WhenAvatarIsEmpty() {
    User user = User.builder()
      .avatar("   ")
      .account(Account.builder().email("test@example.com").role(Role.USER).build())
      .build();

    UserProfileDTO dto = userMapper.toUserProfileDTO(user);

    assertNull(dto.getAvatar());
  }

  @Test
  void toUserProfileDTO_ShouldReturnNullAvatar_WhenMinioServiceThrowsException() throws Exception {
    String avatarFileName = "avatar.png";
    when(minioService.generatePresignedUrl(avatarFileName)).thenThrow(
      new MinioException("Minio error")
    );

    User user = User.builder()
      .avatar(avatarFileName)
      .account(Account.builder().email("test@example.com").role(Role.USER).build())
      .build();

    UserProfileDTO dto = userMapper.toUserProfileDTO(user);

    assertNull(dto.getAvatar());
  }
}
