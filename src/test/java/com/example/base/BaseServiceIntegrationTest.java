package com.example.base;

import com.example.modules.auth.entities.Account;
import com.example.modules.users.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class BaseServiceIntegrationTest extends BaseIntegrationTest {

  @BeforeEach
  protected void setup() {
    Account account = Account.builder()
      .email("email@example.com")
      .password(passwordEncoder.encode("password@123456"))
      .build();
    User user = User.builder().account(account).build();

    accountsRepository.save(account);
    usersRepository.save(user);
  }

  @AfterEach
  protected void cleanup() {
    usersRepository.deleteAll();
    accountsRepository.deleteAll();
  }
}
