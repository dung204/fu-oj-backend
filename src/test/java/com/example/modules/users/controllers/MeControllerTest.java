package com.example.modules.users.controllers;

import static com.example.base.utils.AppRoutes.ME_PREFIX;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.base.BaseControllerTest;
import com.example.modules.minio.services.MinioService;
import com.example.modules.posts.services.PostsService;
import com.example.modules.users.dtos.UpdateProfileDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import com.example.modules.users.services.UsersService;
import com.example.modules.users.utils.UserMapper;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(MeController.class)
public class MeControllerTest extends BaseControllerTest {

  @MockitoBean
  private PostsService postsService;

  @MockitoBean
  private UsersService usersService;

  @MockitoBean
  private UserMapper userMapper;

  @MockitoBean
  private MinioService minioService;

  @Test
  void getProfileOfCurrentUser_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse()
    throws Exception {
    mockMvc
      .perform(get(ME_PREFIX + "/profile").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void getProfileOfCurrentUser_WhenUserIsNotLoggedIn_ShouldReturnOkResponse() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();
    UserProfileDTO mockUserProfile = createMockUserProfile(mockUser);

    when(userMapper.toUserProfileDTO(mockUser)).thenReturn(mockUserProfile);

    mockMvc
      .perform(
        get(ME_PREFIX + "/profile")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(mockUser.getId()))
      .andExpect(jsonPath("$.data.email").value(mockUser.getAccount().getEmail()))
      .andExpect(jsonPath("$.data.firstName").value(mockUser.getFirstName()))
      .andExpect(jsonPath("$.data.lastName").value(mockUser.getLastName()))
      .andExpect(jsonPath("$.data.role").value(mockUser.getAccount().getRole().getValue()));
  }

  @Test
  void updateUserProfile_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of("First"))
      .lastName(JsonNullable.of("Last"))
      .build();

    mockMvc
      .perform(
        patch(ME_PREFIX + "/profile")
          .contentType(MediaType.APPLICATION_JSON)
          .content(objectMapper.writeValueAsString(updateProfileDTO))
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void updateUserProfile_WhenUserIsLoggedInWithValidData_ShouldReturnOkResponse() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();

    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of("First"))
      .lastName(JsonNullable.of("Last"))
      .build();

    UserProfileDTO updatedUserProfile = createMockUserProfile(mockUser);
    updatedUserProfile.setFirstName(updateProfileDTO.getFirstName().get());
    updatedUserProfile.setLastName(updateProfileDTO.getLastName().get());

    when(usersService.updateProfile(mockUser, updateProfileDTO)).thenReturn(updatedUserProfile);

    mockMvc
      .perform(
        patch(ME_PREFIX + "/profile")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
          .content(objectMapper.writeValueAsString(updateProfileDTO))
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(updatedUserProfile.getId()))
      .andExpect(jsonPath("$.data.email").value(updatedUserProfile.getEmail()))
      .andExpect(jsonPath("$.data.firstName").value(updatedUserProfile.getFirstName()))
      .andExpect(jsonPath("$.data.lastName").value(updatedUserProfile.getLastName()))
      .andExpect(jsonPath("$.data.role").value(updatedUserProfile.getRole()));
  }

  @Test
  void updateUserProfile_WhenUserIsLoggedInWithInvalidData_ShouldReturnBadRequestResponse()
    throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    UpdateProfileDTO invalidUpdateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of(""))
      .lastName(JsonNullable.of(""))
      .build();

    mockMvc
      .perform(
        patch(ME_PREFIX + "/profile")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Authorization", "Bearer " + mockAccessToken)
          .content(objectMapper.writeValueAsString(invalidUpdateProfileDTO))
      )
      .andExpect(status().isBadRequest());
  }

  @Test
  void updateUserAvatar_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile(
      "file",
      "avatar.jpg",
      "image/jpeg",
      "test image content".getBytes()
    );

    mockMvc
      .perform(
        multipart(ME_PREFIX + "/avatar")
          .file(mockFile)
          .with(request -> {
            request.setMethod("PATCH");
            return request;
          })
      )
      .andExpect(status().isUnauthorized());
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithValidFile_ShouldReturnOkResponse() throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();
    User mockUser = payload.user();

    MockMultipartFile mockFile = new MockMultipartFile(
      "file",
      "avatar.jpg",
      "image/jpeg",
      "test image content".getBytes()
    );

    UserProfileDTO updatedUserProfile = createMockUserProfile(mockUser);

    when(usersService.updateAvatar(mockUser, mockFile)).thenReturn(updatedUserProfile);

    mockMvc
      .perform(
        multipart(ME_PREFIX + "/avatar")
          .file(mockFile)
          .header("Authorization", "Bearer " + mockAccessToken)
          .with(request -> {
            request.setMethod("PATCH");
            return request;
          })
      )
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.status").value(200))
      .andExpect(jsonPath("$.message").isString())
      .andExpect(jsonPath("$.data.id").value(updatedUserProfile.getId()))
      .andExpect(jsonPath("$.data.email").value(updatedUserProfile.getEmail()))
      .andExpect(jsonPath("$.data.firstName").value(updatedUserProfile.getFirstName()))
      .andExpect(jsonPath("$.data.lastName").value(updatedUserProfile.getLastName()))
      .andExpect(jsonPath("$.data.role").value(updatedUserProfile.getRole()));
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithNoFile_ShouldReturnBadRequestResponse()
    throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    mockMvc
      .perform(
        multipart(ME_PREFIX + "/avatar")
          .header("Authorization", "Bearer " + mockAccessToken)
          .with(request -> {
            request.setMethod("PATCH");
            return request;
          })
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithInvalidFileType_ShouldReturnBadRequestResponse()
    throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    MockMultipartFile mockFile = new MockMultipartFile(
      "file",
      "document.pdf",
      "application/pdf",
      "test pdf content".getBytes()
    );

    mockMvc
      .perform(
        multipart(ME_PREFIX + "/avatar")
          .file(mockFile)
          .header("Authorization", "Bearer " + mockAccessToken)
          .with(request -> {
            request.setMethod("PATCH");
            return request;
          })
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithFileTooLarge_ShouldReturnBadRequestResponse()
    throws Exception {
    MockUserLoginPayload payload = mockUserLogin();
    String mockAccessToken = payload.accessToken();

    // Create a file larger than 1MB
    byte[] largeFileContent = new byte[2 * 1024 * 1024]; // 2MB
    MockMultipartFile mockFile = new MockMultipartFile(
      "file",
      "large-avatar.jpg",
      "image/jpeg",
      largeFileContent
    );

    mockMvc
      .perform(
        multipart(ME_PREFIX + "/avatar")
          .file(mockFile)
          .header("Authorization", "Bearer " + mockAccessToken)
          .with(request -> {
            request.setMethod("PATCH");
            return request;
          })
      )
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.message").isString());
  }
}
