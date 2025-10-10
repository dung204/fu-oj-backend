package com.example.modules.users.controllers;

import static com.example.base.utils.AppRoutes.ME_PREFIX;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.base.BaseControllerIntegrationTest;
import com.example.base.dtos.ErrorResponseDTO;
import com.example.base.dtos.SuccessResponseDTO;
import com.example.modules.auth.services.JwtService;
import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.users.dtos.UpdateProfileDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

public class MeControllerIntegrationTest extends BaseControllerIntegrationTest {

  @Autowired
  protected JwtService jwtService;

  @Test
  void getProfileOfCurrentUser_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse()
    throws Exception {
    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      ME_PREFIX + "/profile",
      HttpMethod.GET,
      HttpEntity.EMPTY,
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void getProfileOfCurrentUser_WhenUserIsNotLoggedIn_ShouldReturnOkResponse() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.put(HttpHeaders.AUTHORIZATION, List.of("Bearer " + accessToken));

    ResponseEntity<SuccessResponseDTO<UserProfileDTO>> response = restTemplate.exchange(
      ME_PREFIX + "/profile",
      HttpMethod.GET,
      new HttpEntity<>(null, headers),
      new ParameterizedTypeReference<SuccessResponseDTO<UserProfileDTO>>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    UserProfileDTO userProfile = response.getBody().getData();
    assertNotNull(userProfile);

    assertEquals(user.getId(), userProfile.getId());
    assertEquals(user.getAccount().getEmail(), userProfile.getEmail());
    assertEquals(user.getAccount().getRole().getValue(), userProfile.getRole());
  }

  @Test
  void updateUserProfile_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of("First"))
      .lastName(JsonNullable.of("Last"))
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      ME_PREFIX + "/profile",
      HttpMethod.PATCH,
      new HttpEntity<>(updateProfileDTO),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updateUserProfile_WhenUserIsLoggedInWithValidData_ShouldReturnOkResponse() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of("First"))
      .lastName(JsonNullable.of("Last"))
      .build();

    ResponseEntity<SuccessResponseDTO<UserProfileDTO>> response = restTemplate.exchange(
      ME_PREFIX + "/profile",
      HttpMethod.PATCH,
      new HttpEntity<>(updateProfileDTO, headers),
      new ParameterizedTypeReference<SuccessResponseDTO<UserProfileDTO>>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    UserProfileDTO userProfile = response.getBody().getData();
    assertNotNull(userProfile);

    assertEquals(user.getId(), userProfile.getId());
    assertEquals(user.getAccount().getEmail(), userProfile.getEmail());
    assertEquals(user.getAccount().getRole().getValue(), userProfile.getRole());
    assertEquals(updateProfileDTO.getFirstName().get(), userProfile.getFirstName());
    assertEquals(updateProfileDTO.getLastName().get(), userProfile.getLastName());
  }

  @Test
  void updateUserProfile_WhenUserIsLoggedInWithInvalidData_ShouldReturnBadRequestResponse()
    throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    UpdateProfileDTO invalidUpdateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of(""))
      .lastName(JsonNullable.of(""))
      .build();

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      ME_PREFIX + "/profile",
      HttpMethod.PATCH,
      new HttpEntity<>(invalidUpdateProfileDTO, headers),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updateUserAvatar_WhenUserIsNotLoggedIn_ShouldReturnUnauthorizedResponse() throws Exception {
    MultipartFile mockFile = new MockMultipartFile(
      "file",
      "avatar.jpg",
      "image/jpeg",
      "test image content".getBytes()
    );

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", mockFile.getResource());

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      ME_PREFIX + "/profile",
      HttpMethod.PATCH,
      new HttpEntity<>(body),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(401, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithValidFile_ShouldReturnOkResponse() throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    MultipartFile mockFile = new MockMultipartFile(
      "file",
      "avatar.jpg",
      "image/jpeg",
      "test image content".getBytes()
    );

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", mockFile.getResource());

    ResponseEntity<SuccessResponseDTO<UserProfileDTO>> response = restTemplate.exchange(
      ME_PREFIX + "/avatar",
      HttpMethod.PATCH,
      new HttpEntity<>(body, headers),
      new ParameterizedTypeReference<SuccessResponseDTO<UserProfileDTO>>() {}
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(200, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());

    UserProfileDTO userProfile = response.getBody().getData();
    assertNotNull(userProfile);

    assertEquals(user.getId(), userProfile.getId());
    assertEquals(user.getAccount().getEmail(), userProfile.getEmail());
    assertEquals(user.getAccount().getRole().getValue(), userProfile.getRole());

    MinioFileResponse avatar = userProfile.getAvatar();
    assertNotNull(avatar);

    assertTrue(avatar.getFileName().contains("avatars/%s".formatted(user.getId())));
    assertTrue(avatar.getFileName().contains("avatar.jpg"));
    assertDoesNotThrow(() -> URI.create(avatar.getUrl()));
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithNoFile_ShouldReturnBadRequestResponse()
    throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    ResponseEntity<SuccessResponseDTO<UserProfileDTO>> response = restTemplate.exchange(
      ME_PREFIX + "/avatar",
      HttpMethod.PATCH,
      new HttpEntity<>(new LinkedMultiValueMap<>(), headers),
      new ParameterizedTypeReference<SuccessResponseDTO<UserProfileDTO>>() {}
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithInvalidFileType_ShouldReturnBadRequestResponse()
    throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    MultipartFile mockFile = new MockMultipartFile(
      "file",
      "document.pdf",
      "application/pdf",
      "test pdf content".getBytes()
    );

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", mockFile.getResource());

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      ME_PREFIX + "/avatar",
      HttpMethod.PATCH,
      new HttpEntity<>(body, headers),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }

  @Test
  void updateUserAvatar_WhenUserIsLoggedInWithFileTooLarge_ShouldReturnBadRequestResponse()
    throws Exception {
    User user = getUser();
    String accessToken = jwtService.generateAccessToken(user);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

    // Create a file larger than 1MB
    byte[] largeFileContent = new byte[2 * 1024 * 1024]; // 2MB
    MultipartFile mockFile = new MockMultipartFile(
      "file",
      "large-avatar.jpg",
      "image/jpeg",
      largeFileContent
    );

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", mockFile.getResource());

    ResponseEntity<ErrorResponseDTO> response = restTemplate.exchange(
      ME_PREFIX + "/avatar",
      HttpMethod.PATCH,
      new HttpEntity<>(body, headers),
      ErrorResponseDTO.class
    );

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(400, response.getBody().getStatus());
    assertFalse(response.getBody().getMessage().isEmpty());
  }
}
