package com.example.modules.users.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.example.base.BaseServiceIntegrationTest;
import com.example.modules.users.dtos.UpdateProfileDTO;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import java.net.URI;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class UsersServiceIntegrationTest extends BaseServiceIntegrationTest {

  @Autowired
  private UsersService usersService;

  @Test
  void updateProfile_ShouldAssignFieldsAndReturnUserProfileDTO() {
    User user = getUser();
    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder()
      .firstName(JsonNullable.of("update first name"))
      .lastName(JsonNullable.of("update last name"))
      .build();

    UserProfileDTO result = usersService.updateProfile(user, updateProfileDTO);

    assertNotNull(result);
    assertEquals(result.getFirstName(), user.getFirstName());
    assertEquals(result.getLastName(), user.getLastName());
  }

  @Test
  void updateProfile_shouldHandleNullUpdateProfileDTO() {
    User user = getUser();
    UpdateProfileDTO updateProfileDTO = UpdateProfileDTO.builder().build();

    UserProfileDTO result = usersService.updateProfile(user, updateProfileDTO);

    assertNotNull(result);
    assertEquals(result.getFirstName(), user.getFirstName());
    assertEquals(result.getLastName(), user.getLastName());
  }

  @Test
  void updateAvatar_ShouldUploadFileAndReturnUserProfileDTO() throws Exception {
    User user = getUser();
    MultipartFile file = new MockMultipartFile(
      "avatar",
      "avatar.png",
      "image/png",
      "content".getBytes()
    );

    UserProfileDTO result = usersService.updateAvatar(user, file);

    assertNotNull(result);
    assertNotNull(result.getAvatar());
    assertTrue(result.getAvatar().getFileName().contains(file.getOriginalFilename()));
    assertDoesNotThrow(() -> URI.create(result.getAvatar().getUrl()));
  }
}
