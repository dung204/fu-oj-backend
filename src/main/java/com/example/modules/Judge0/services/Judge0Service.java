package com.example.modules.Judge0.services;

import com.example.modules.Judge0.enums.Judge0Status;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class Judge0Service {

  private final WebClient webClient;
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

  public List<String> createBatchSubmission(
    String sourceCode,
    String languageId,
    List<String> testInputs
  ) {
    String url = JUDGE0_API + "/submissions/batch?base64_encoded=false&wait=false";

    // Build request body
    List<Map<String, Object>> submissions = new ArrayList<>();
    for (String input : testInputs) {
      Map<String, Object> submission = new HashMap<>();
      submission.put("source_code", sourceCode);
      submission.put("language_id", Integer.parseInt(languageId));
      submission.put("stdin", input.replace("\\n", "\n"));
      log.info("==== ACTUAL STDIN ====");
      log.info(input);
      log.info("======================");
      submission.put("callback_url", CALLBACK_URL);
      submissions.add(submission);
    }

    Map<String, Object> requestBody = Map.of("submissions", submissions);

    log.info("Judge0 Batch Request: {}", requestBody);

    return this.requestToJudge0(url, requestBody);
  }

  public List<String> createBatchSubmissionBase64(
    String sourceCode,
    String languageId,
    List<String> testInputs
  ) {
    String url = JUDGE0_API + "/submissions/batch?base64_encoded=true&wait=false";

    // Encode source code sang Base64
    String encodedSourceCode = Base64.getEncoder().encodeToString(
      sourceCode.getBytes(StandardCharsets.UTF_8)
    );

    List<Map<String, Object>> submissions = new ArrayList<>();

    log.info("testInputs BEFORE: {}", testInputs);

    for (String input : testInputs) {
      // Chuẩn hóa xuống dòng & encode stdin
      String normalizedInput = input.replace("\\n", "\n");
      String encodedInput = Base64.getEncoder().encodeToString(
        normalizedInput.getBytes(StandardCharsets.UTF_8)
      );

      log.info("==== ACTUAL STDIN BEFORE ENCODE ====");
      log.info(normalizedInput);
      log.info("==== ENCODED STDIN (Base64) ====");
      log.info(encodedInput);
      log.info("======================");

      Map<String, Object> submission = new HashMap<>();
      submission.put("source_code", encodedSourceCode);
      submission.put("language_id", Integer.parseInt(languageId));
      submission.put("stdin", encodedInput);
      submission.put("callback_url", CALLBACK_URL);
      submissions.add(submission);
    }

    Map<String, Object> requestBody = Map.of("submissions", submissions);

    log.info("Judge0 Batch Request (Base64 Encoded): {}", requestBody);

    return this.requestToJudge0(url, requestBody);
  }

  /**
   * Hiện tại đang triển khai bằng callback nên hàm này chưa dùng tới
   */
  public Map<String, Object> getSubmission(String token) {
    String url = JUDGE0_API + "/submissions/" + token + "?base64_encoded=false";

    log.info("Fetching Judge0 Submission with token: {}", token);

    try {
      // Gửi request đến Judge0
      String responseBody = webClient.get().uri(url).retrieve().bodyToMono(String.class).block(); // giữ hành vi đồng bộ (như RestTemplate)

      log.info("Judge0 Get Submission Response: {}", responseBody);

      // Parse JSON response thành Map
      Map<String, Object> body = objectMapper.readValue(responseBody, new TypeReference<>() {});

      // Lấy thông tin status
      Map<String, Object> statusMap = (Map<String, Object>) body.get("status");
      int statusId = (int) statusMap.get("id");
      Judge0Status status = Judge0Status.fromId(statusId);

      // Log trạng thái
      log.info("Judge0 Status: {} ({})", statusId, status.getDescription());

      // Kiểm tra kết quả thực thi
      if (status == Judge0Status.ACCEPTED) {
        log.info("Judge0 submission accepted");
        return body;
      } else {
        log.warn("Judge0 execution error: {}", status.getDescription());
      }
    } catch (Exception e) {
      log.error("Error fetching submission {} from Judge0", token, e);
      throw new ResponseStatusException(
        HttpStatus.BAD_GATEWAY,
        "Error fetching submission from Judge0"
      );
    }
    return Collections.emptyMap();
  }

  private List<String> requestToJudge0(String url, Map<String, Object> requestBody) {
    // Gửi request bằng WebClient
    String responseBody = webClient
      .post()
      .uri(url)
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestBody)
      .retrieve()
      .bodyToMono(String.class)
      .block(); // block() để giữ hành vi đồng bộ như RestTemplate

    log.info("Judge0 Batch Response: {}", responseBody);

    // Parse response
    try {
      List<Map<String, Object>> submissionList = objectMapper.readValue(
        responseBody,
        new TypeReference<List<Map<String, Object>>>() {}
      );

      return submissionList
        .stream()
        .map(item -> item.get("token").toString())
        .collect(Collectors.toList());
    } catch (Exception e) {
      log.error("Failed to parse Judge0 response", e);
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to parse Judge0 response");
    }
  }
}

// cơ chế xử lí server ko nhận được callback
// base 64: xử lí những thằng code gửi lên ( có kí tự đặc biệt mà judge0 ko hiểu được )
// exercise: thêm level cho bài tập
// user: thêm điểm, rank
