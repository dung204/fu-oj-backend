package com.example.base;

import com.example.modules.auth.entities.Account;
import com.example.modules.minio.dtos.MinioFileResponse;
import com.example.modules.users.dtos.UserProfileDTO;
import com.example.modules.users.entities.User;
import java.time.Instant;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
public class BaseServiceTest {

  protected Account getMockAccount() {
    String email = "test@example.com";
    String password = "password@123456";

    Account account = Account.builder().email(email).password(password).build();
    return account;
  }

  protected User getMockUser() {
    Instant currentTimestamp = Instant.now();

    return User.builder()
      .id("user-123")
      .account(getMockAccount())
      .firstName("John")
      .lastName("Doe")
      .createdTimestamp(currentTimestamp)
      .updatedTimestamp(currentTimestamp)
      .build();
  }

  protected UserProfileDTO getMockUserProfile() {
    User user = getMockUser();

    return UserProfileDTO.builder()
      .id(user.getId())
      .email(user.getAccount().getEmail())
      .firstName(user.getFirstName())
      .lastName(user.getLastName())
      .avatar(
        user.getAvatar() == null
          ? null
          : MinioFileResponse.builder().fileName(user.getAvatar()).url("image url").build()
      )
      .role(user.getAccount().getRole().getValue())
      .createdTimestamp(user.getCreatedTimestamp().toString())
      .updatedTimestamp(user.getUpdatedTimestamp().toString())
      .build();
  }
}
