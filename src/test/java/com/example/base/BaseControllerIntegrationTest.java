package com.example.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@Sql(
  statements = {
    "INSERT INTO accounts (id, created_timestamp, updated_timestamp, email, password, role) VALUES ('d449ffc6-7573-4781-8c72-020ab5f435ea', NOW(), NOW(), 'email@example.com', '$2a$10$qLGDd6oa1eZxcBvA3sYIROBeN2nmcvXBONafYzKiLwTKaAWLqL.PG', 'USER')",
    "INSERT INTO users (id, account_id, created_timestamp, updated_timestamp) VALUES ('6488a2d2-daed-443e-94f1-d86529c1d46f' ,'d449ffc6-7573-4781-8c72-020ab5f435ea', NOW(), NOW())",
  },
  executionPhase = ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
  statements = { "DELETE FROM users", "DELETE FROM accounts" },
  executionPhase = ExecutionPhase.AFTER_TEST_METHOD
)
public class BaseControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  protected TestRestTemplate restTemplate;
}
