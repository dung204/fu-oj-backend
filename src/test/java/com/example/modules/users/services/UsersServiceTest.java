package com.example.modules.users.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.base.BaseServiceTest;
import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.minio.services.MinioService;
import com.example.modules.users.dtos.UpdateProfileDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.repositories.UsersRepository;
import com.example.modules.users.utils.UserMapper;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class UsersServiceTest extends BaseServiceTest {

  @Mock
  private UsersRepository usersRepository;

  @Mock
  private MinioService minioService;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UsersService usersService;

  @Test
  void updateProfile_ShouldAssignFieldsAndReturnUserProfileDTO() {
    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of("update first name"))
      .lastName(JsonNullable.of("update last name"))
      .build();
    User mockUser = getMockUser();
    UserProfileDTO userProfileDTO = getMockUserProfile();
    userProfileDTO.setFirstName("update first name");
    userProfileDTO.setLastName("update last name");

    // Simulate repository save and mapping
    when(usersRepository.save(mockUser)).thenReturn(mockUser);
    when(userMapper.toUserProfileDTO(mockUser)).thenReturn(userProfileDTO);

    UserProfileDTO result = usersService.updateProfile(mockUser, updateProfileDTO);

    verify(usersRepository).save(mockUser);
    verify(userMapper).toUserProfileDTO(mockUser);
    assertEquals(userProfileDTO, result);
  }

  @Test
  void updateProfile_shouldHandleNullUpdateProfileDTO() {
    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder().build();
    User mockUser = getMockUser();
    UserProfileDTO userProfileDTO = getMockUserProfile();

    // Simulate repository save and mapping
    when(usersRepository.save(mockUser)).thenReturn(mockUser);
    when(userMapper.toUserProfileDTO(mockUser)).thenReturn(userProfileDTO);

    UserProfileDTO result = usersService.updateProfile(mockUser, updateProfileDTO);

    verify(usersRepository).save(mockUser);
    verify(userMapper).toUserProfileDTO(mockUser);
    assertEquals(userProfileDTO, result);
  }

  @Test
  void updateAvatar_ShouldUploadFileAndReturnUserProfileDTO() throws Exception {
    User mockUser = getMockUser();
    MultipartFile mockFile = new MockMultipartFile(
      "avatar",
      "avatar.png",
      "image/png",
      "content".getBytes()
    );
    MinioFileResponse minioFileResponse = MinioFileResponse.builder()
      .fileName("avatar_123.png")
      .url("url")
      .build();

    User savedUser = getMockUser();
    savedUser.setAvatar("avatars/%s".formatted(mockUser.getId()));

    UserProfileDTO userProfileDTO = getMockUserProfile();
    userProfileDTO.setAvatar(minioFileResponse);

    when(minioService.uploadFile(mockFile, "avatars/%s".formatted(mockUser.getId()))).thenReturn(
      minioFileResponse
    );
    when(usersRepository.save(mockUser)).thenReturn(savedUser);
    when(userMapper.toUserProfileDTO(savedUser)).thenReturn(userProfileDTO);

    UserProfileDTO result = usersService.updateAvatar(mockUser, mockFile);

    verify(minioService).uploadFile(mockFile, "avatars/%s".formatted(mockUser.getId()));
    verify(usersRepository).save(mockUser);
    verify(userMapper).toUserProfileDTO(savedUser);
    assertEquals(userProfileDTO, result);
  }

  @Test
  void updateAvatar_ShouldThrowException_WhenMinioServiceFails() throws Exception {
    User mockUser = getMockUser();

    MultipartFile mockFile = new MockMultipartFile(
      "avatar",
      "avatar.png",
      "image/png",
      "content".getBytes()
    );

    when(minioService.uploadFile(mockFile, "avatars/%s".formatted(mockUser.getId()))).thenThrow(
      new MinioException("Minio error")
    );

    assertThrows(MinioException.class, () -> usersService.updateAvatar(mockUser, mockFile));
  }
}
