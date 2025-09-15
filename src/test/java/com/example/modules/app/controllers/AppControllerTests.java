package com.example.modules.app.controllers;

import static com.example.base.utils.AppRoutes.API_PREFIX;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.base.BaseControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AppController.class)
public class AppControllerTests extends BaseControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void checkHealth_shouldReturnOkResponse() throws Exception {
    mockMvc
      .perform(get(API_PREFIX + "/health"))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.message").value("OK"));
  }
}
