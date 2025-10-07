package com.example.modules.Judge0.services;

import com.example.modules.Judge0.enums.Judge0Status;
import com.example.modules.Judge0.exceptions.Judge0Exception;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class Judge0Service {

  private final RestTemplate restTemplate = new RestTemplate();
  private final ObjectMapper objectMapper;

  @Value("${judge0.judge0-api-url}")
  private String JUDGE0_API;

  @Value("${judge0.callback-url}")
  private String CALLBACK_URL;

  /**
   * stdin: input from system ( test case )
   * sourceCode: code from user
   * languageId: programming language id from Judge0
   * hiểu đúng về JUDGE0: chỉ có nhiệm vụ: biên dịch, chạy code, trả stdout/stderr + status (compile error, runtime error, tle, …).
   * còn việc so sánh output với expected output, tính điểm, v.v… là do hệ thống của mình làm
   */
  public String createSubmission(String sourceCode, String languageId, String stdin) {
    String url = JUDGE0_API + "/submissions?base64_encoded=false&wait=false";

    Map<String, Object> body = new HashMap<>();
    body.put("source_code", sourceCode);
    body.put("language_id", Integer.parseInt(languageId));
    body.put("stdin", stdin);
    body.put("callback_url", CALLBACK_URL);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    try {
      ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

      if (response.getBody() != null) {
        return (String) response.getBody().get("token");
      } else {
        throw new Judge0Exception(
          HttpStatus.BAD_GATEWAY,
          "Judge0 API error: " + response.getStatusCode()
        );
      }
    } catch (Exception e) {
      log.error("Judge0 API error", e);
      throw new Judge0Exception(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Error when creating submission with Judge0"
      );
    }
  }

  public List<String> createBatchSubmission(
    String sourceCode,
    String languageId,
    List<String> testInputs
  ) {
    String url = JUDGE0_API + "/submissions/batch?base64_encoded=false&wait=false";

    List<Map<String, Object>> submissions = new ArrayList<>();
    for (String input : testInputs) {
      Map<String, Object> submission = new HashMap<>();
      submission.put("source_code", sourceCode);
      submission.put("language_id", Integer.parseInt(languageId));
      submission.put("stdin", input);
      submission.put("callback_url", CALLBACK_URL);
      submissions.add(submission);
    }

    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("submissions", submissions);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    log.info("Judge0 Batch Request Body BEFORE: {}", requestBody);
    String jsonBody;
    try {
      // Ép thành JSON string thật
      jsonBody = objectMapper.writeValueAsString(requestBody).replace("\\\\", "\\");
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize requestBody", e);
    }

    log.info("Judge0 Batch Request JSON AFTER: {}", jsonBody);

    HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
    log.info("Judge0 Batch Response: {}", response.getBody());

    try {
      // Parse trực tiếp về List
      List<Map<String, Object>> parsedList = objectMapper.readValue(
        response.getBody(),
        new TypeReference<List<Map<String, Object>>>() {}
      );

      // Lấy các token
      return parsedList
        .stream()
        .map(item -> item.get("token").toString())
        .collect(Collectors.toList());
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse Judge0 response", e);
    }
  }

  /**
   * Hiện tại đang triển khai bằng callback nên hàm này chưa dùng tới
   */
  public Map<String, Object> getSubmission(String token) {
    String url = JUDGE0_API + "/submissions/" + token + "?base64_encoded=false";

    try {
      ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

      if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
        Map<String, Object> body = response.getBody();

        // đọc status.id
        Map<String, Object> statusMap = (Map<String, Object>) body.get("status");
        int statusId = (int) statusMap.get("id");
        Judge0Status status = Judge0Status.fromId(statusId);

        if (status == Judge0Status.ACCEPTED) {
          return body;
        } else {
          throw new Judge0Exception(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "Judge0 Execution Error: " + status.getDescription()
          );
        }
      } else {
        throw new Judge0Exception(
          HttpStatus.BAD_GATEWAY,
          "Judge0 API error: " + response.getStatusCode()
        );
      }
    } catch (Exception e) {
      log.error("Error fetching submission {} from Judge0", token, e);
      throw new Judge0Exception(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Error fetching submission from Judge0"
      );
    }
  }
}

// cơ chế xử lí server ko nhận được callback
// base 64: xử lí những thằng code gửi lên ( có kí tự đặc biệt mà judge0 ko hiểu được )
// exercise: thêm level cho bài tập
// user: thêm điểm, rank
