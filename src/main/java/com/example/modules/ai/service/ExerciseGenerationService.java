package com.example.modules.ai.service;

import com.example.modules.ai.dtos.request.ExerciseGenerationRequest;
import com.example.modules.ai.dtos.response.ExerciseGenerationResponse;
import com.example.modules.ai.dtos.response.ExercisePreviewDTO;
import com.example.modules.exercises.entities.Exercise;
import com.example.modules.exercises.repositories.ExercisesRepository;
import com.example.modules.exercises.utils.ExercisesSpecification;
import com.example.modules.test_cases.dtos.TestCaseRequestDTO;
import com.example.modules.topics.entities.Topic;
import com.example.modules.topics.repositories.TopicsRepository;
import com.example.modules.topics.utils.TopicsSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExerciseGenerationService {

  private final ChatClient chatClient;
  private final ObjectMapper objectMapper;
  private final TopicsRepository topicsRepository;
  private final ExercisesRepository exercisesRepository;

  public ExerciseGenerationService(
    ChatClient.Builder builder,
    ObjectMapper objectMapper,
    TopicsRepository topicsRepository,
    ExercisesRepository exercisesRepository
  ) {
    this.chatClient = builder.build();
    this.objectMapper = objectMapper;
    this.topicsRepository = topicsRepository;
    this.exercisesRepository = exercisesRepository;
  }

  public ExerciseGenerationResponse generateExercises(ExerciseGenerationRequest request) {
    try {
      // Tìm topic theo name hoặc ID
      Topic topic = topicsRepository.findById(request.getTopic()).orElse(null);
      if (topic == null) {
        throw new RuntimeException("Không tìm thấy topic: " + request.getTopic());
      }

      // Query sample exercises theo topic
      List<Exercise> sampleExercises = getSampleExercisesByTopic(topic.getId());

      // Tạo prompt với context + knowledge + constraints
      String systemPrompt = buildSystemPrompt();
      log.info("System Prompt: {}", systemPrompt);
      String userPrompt = buildUserPrompt(request, topic, sampleExercises);
      log.info("User Prompt: {}", userPrompt);

      UserMessage userMessage = new UserMessage(userPrompt);
      Prompt prompt = new Prompt(new SystemMessage(systemPrompt), userMessage);
      String response = chatClient.prompt(prompt).call().content();

      // Parse JSON response và set topicIds
      return parseAIResponse(response, topic.getId());
    } catch (NonTransientAiException e) {
      log.error("AI service error: {}", e.getMessage(), e);
      throw new RuntimeException("Lỗi khi tạo bài tập. Vui lòng thử lại sau.", e);
    } catch (Exception e) {
      log.error("Unexpected error in exercise generation: {}", e.getMessage(), e);
      throw new RuntimeException("Đã xảy ra lỗi không mong muốn. Vui lòng thử lại sau.", e);
    }
  }

  private List<Exercise> getSampleExercisesByTopic(String topicId) {
    // Lấy tối đa 2 bài tập mẫu về topic này (chỉ lấy PUBLIC và latest version)
    return exercisesRepository
      .findAll(
        ExercisesSpecification.builder()
          .hasOneOfTopics(List.of(topicId))
          .publicOnly()
          .onlyLatestVersion()
          .notDeleted()
          .build(),
        PageRequest.of(0, 2)
      )
      .getContent();
  }

  private String buildSystemPrompt() {
    return (
      "You are an AI assistant specialized in creating programming exercises for teachers. " +
      "Your task is to generate EXACTLY N programming exercises in JSON array format. " +
      "Return ONLY valid JSON array, NO explanatory text, NO markdown, NO code blocks. " +
      "The response must be a pure JSON array starting with '[' and ending with ']'."
    );
  }

  private String buildUserPrompt(
    ExerciseGenerationRequest request,
    Topic topic,
    List<Exercise> sampleExercises
  ) {
    StringBuilder promptBuilder = new StringBuilder();

    promptBuilder
      .append("Hãy tạo EXACTLY ")
      .append(request.getNumberOfExercise())
      .append(" bài tập lập trình theo đúng yêu cầu sau:\n\n");

    promptBuilder.append("1. Chủ đề: ").append(topic.getName()).append("\n\n");

    promptBuilder
      .append("2. Mỗi bài phải thuộc đúng độ khó được yêu cầu (")
      .append(String.join(", ", request.getLevel()))
      .append(")\n\n");

    promptBuilder.append(
      "3. Trả về kết quả DUY NHẤT ở dạng JSON array, KHÔNG được bao gồm text giải thích, KHÔNG dùng markdown, KHÔNG bao code block.\n\n"
    );

    promptBuilder.append("Định dạng mỗi object trong JSON phải EXACTLY như sau:\n\n");
    promptBuilder.append("{\n");
    promptBuilder.append("  \"code\": \"EXxxx\",\n");
    promptBuilder.append("  \"title\": \"...\",\n");
    promptBuilder.append("  \"description\": \"...\",\n");
    promptBuilder.append("  \"maxSubmissions\": 0,\n");
    promptBuilder.append("  \"topicIds\": [],\n");
    promptBuilder.append("  \"visibility\": \"DRAFT\",\n");
    promptBuilder.append("  \"timeLimit\": 0.2,\n");
    promptBuilder.append("  \"memory\": 65000,\n");
    promptBuilder.append("  \"difficulty\": \"EASY/MEDIUM/HARD\",\n");
    promptBuilder.append("  \"solution\": \"code Java, không bao code block\",\n");
    promptBuilder.append("  \"testCases\": [\n");
    promptBuilder.append("    {\n");
    promptBuilder.append("      \"input\": \"...\",\n");
    promptBuilder.append("      \"output\": \"...\",\n");
    promptBuilder.append("      \"isPublic\": true/false\n");
    promptBuilder.append("    }\n");
    promptBuilder.append("  ]\n");
    promptBuilder.append("}\n\n");

    promptBuilder.append("QUAN TRỌNG:\n");
    promptBuilder.append("- Mỗi bài có tối thiểu 3 test cases (ít nhất 1 private)\n");
    promptBuilder.append("- input/output KHÔNG được chứa mô tả thêm, chỉ raw values\n");
    promptBuilder.append("- code trong \"solution\" phải là Java, thuần text, KHÔNG có comment\n");
    promptBuilder.append("- KHÔNG sinh thêm nội dung ngoài JSON\n\n");

    // Thêm bài tập mẫu nếu có (tùy chọn)
    if (!sampleExercises.isEmpty()) {
      promptBuilder.append(
        "Tham khảo (không bắt buộc): Đây là một số bài tập mẫu về chủ đề này:\n"
      );
      for (int i = 0; i < sampleExercises.size() && i < 2; i++) {
        Exercise ex = sampleExercises.get(i);
        promptBuilder.append(String.format("- %s: %s\n", ex.getCode(), ex.getTitle()));
      }
      promptBuilder.append("\n");
    }

    promptBuilder.append("Yêu cầu cụ thể: ").append(request.getPrompt());

    return promptBuilder.toString();
  }

  private ExerciseGenerationResponse parseAIResponse(String response, String topicId)
    throws JsonProcessingException {
    try {
      // Remove markdown code blocks if present
      String cleanedResponse = response.trim();
      if (cleanedResponse.startsWith("```json")) {
        cleanedResponse = cleanedResponse.substring(7);
      }
      if (cleanedResponse.startsWith("```")) {
        cleanedResponse = cleanedResponse.substring(3);
      }
      if (cleanedResponse.endsWith("```")) {
        cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
      }
      cleanedResponse = cleanedResponse.trim();

      // Tìm JSON array trong response (có thể có text trước/sau)
      int startIdx = cleanedResponse.indexOf('[');
      int endIdx = cleanedResponse.lastIndexOf(']');

      if (startIdx != -1 && endIdx != -1 && endIdx > startIdx) {
        cleanedResponse = cleanedResponse.substring(startIdx, endIdx + 1);
      }

      JsonNode jsonNode = objectMapper.readTree(cleanedResponse);

      // Xử lý JSON array trực tiếp
      List<ExercisePreviewDTO> exercises = new ArrayList<>();
      if (jsonNode.isArray()) {
        for (JsonNode exerciseNode : jsonNode) {
          ExercisePreviewDTO exercise = parseExercise(exerciseNode, topicId);
          exercises.add(exercise);
        }
      } else if (jsonNode.has("exercises") && jsonNode.get("exercises").isArray()) {
        // Fallback: nếu vẫn có format {"exercises": [...]}
        for (JsonNode exerciseNode : jsonNode.get("exercises")) {
          ExercisePreviewDTO exercise = parseExercise(exerciseNode, topicId);
          exercises.add(exercise);
        }
      }

      return new ExerciseGenerationResponse(exercises);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse AI response: {}", response, e);
      throw new RuntimeException(
        "Không thể phân tích phản hồi từ AI. Vui lòng thử lại với prompt khác.",
        e
      );
    }
  }

  private ExercisePreviewDTO parseExercise(JsonNode exerciseNode, String topicId) {
    ExercisePreviewDTO exercise = new ExercisePreviewDTO();

    if (exerciseNode.has("code")) {
      exercise.setCode(exerciseNode.get("code").asText());
    }
    if (exerciseNode.has("title")) {
      exercise.setTitle(exerciseNode.get("title").asText());
    }
    if (exerciseNode.has("description")) {
      exercise.setDescription(exerciseNode.get("description").asText());
    }
    if (exerciseNode.has("solution")) {
      exercise.setSolution(exerciseNode.get("solution").asText());
    }
    if (exerciseNode.has("difficulty")) {
      exercise.setDifficulty(exerciseNode.get("difficulty").asText());
    }
    if (exerciseNode.has("timeLimit")) {
      exercise.setTimeLimit(exerciseNode.get("timeLimit").asDouble());
    } else {
      exercise.setTimeLimit(0.2);
    }
    if (exerciseNode.has("memory")) {
      exercise.setMemory(exerciseNode.get("memory").asDouble());
    } else {
      exercise.setMemory(65000.0);
    }
    if (exerciseNode.has("maxSubmissions")) {
      exercise.setMaxSubmissions(exerciseNode.get("maxSubmissions").asInt());
    } else {
      exercise.setMaxSubmissions(0);
    }

    // Set topicIds từ request (mảng chỉ chứa 1 topic)
    if (topicId != null) {
      exercise.setTopicIds(List.of(topicId));
    } else {
      exercise.setTopicIds(new ArrayList<>());
    }

    // Parse visibility, mặc định là DRAFT
    if (exerciseNode.has("visibility")) {
      exercise.setVisibility(exerciseNode.get("visibility").asText());
    } else {
      exercise.setVisibility("DRAFT");
    }

    // Parse test cases
    if (exerciseNode.has("testCases") && exerciseNode.get("testCases").isArray()) {
      List<TestCaseRequestDTO> testCases = new ArrayList<>();
      for (JsonNode testCaseNode : exerciseNode.get("testCases")) {
        TestCaseRequestDTO testCase = new TestCaseRequestDTO();
        if (testCaseNode.has("input")) {
          testCase.setInput(testCaseNode.get("input").asText());
        }
        if (testCaseNode.has("output")) {
          testCase.setOutput(testCaseNode.get("output").asText());
        }
        if (testCaseNode.has("isPublic")) {
          testCase.setIsPublic(testCaseNode.get("isPublic").asBoolean());
        } else {
          testCase.setIsPublic(false);
        }
        testCases.add(testCase);
      }
      exercise.setTestCases(testCases);
    } else {
      exercise.setTestCases(new ArrayList<>());
    }

    return exercise;
  }
}
