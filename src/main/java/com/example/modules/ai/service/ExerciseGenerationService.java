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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
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
      // Validate test cases
      int totalRequired =
        request.getNumberOfPublicTestCases() + request.getNumberOfPrivateTestCases();
      if (totalRequired > request.getTotalTestCasesPerExercise()) {
        throw new RuntimeException(
          String.format(
            "Tổng số test case public (%d) + private (%d) không được vượt quá tổng số test case mỗi bài tập (%d)",
            request.getNumberOfPublicTestCases(),
            request.getNumberOfPrivateTestCases(),
            request.getTotalTestCasesPerExercise()
          )
        );
      }

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

      // Log raw response để info();
      log.info("Raw AI Response: {}", response);

      // Parse JSON response và set topicIds
      return parseAIResponse(response, topic.getId(), request.getVisibility());
    } catch (NonTransientAiException e) {
      log.error("AI service error: {}", e.getMessage(), e);
      throw new RuntimeException("Lỗi khi tạo bài tập. Vui lòng thử lại sau.", e);
    } catch (Exception e) {
      log.error("Unexpected error in exercise generation: {}", e.getMessage(), e);
      throw new RuntimeException(e.getMessage(), e);
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
      "Your ONLY task is to generate EXACTLY N programming exercises in JSON array format based on the user's requirements. " +
      "Return ONLY valid JSON array, NO explanatory text, NO markdown, NO code blocks. " +
      "The response must be a pure JSON array starting with '[' and ending with ']'. " +
      "You must follow the user's requirements strictly regarding topic, difficulty levels, test cases, and formatting."
    );
  }

  private String buildUserPrompt(
    ExerciseGenerationRequest request,
    Topic topic,
    List<Exercise> sampleExercises
  ) {
    // Nếu có prompt từ user thì ưu tiên sử dụng
    if (request.getPrompt() != null && !request.getPrompt().trim().isEmpty()) {
      return buildPromptWithUserInput(request, topic, sampleExercises);
    }

    // Nếu không có prompt thì dùng logic tự động
    return buildAutoPrompt(request, topic, sampleExercises);
  }

  private String buildPromptWithUserInput(
    ExerciseGenerationRequest request,
    Topic topic,
    List<Exercise> sampleExercises
  ) {
    StringBuilder promptBuilder = buildCommonPrompt(request, topic, sampleExercises);
    promptBuilder.append("Yêu cầu cụ thể từ người dùng: ").append(request.getPrompt());
    return promptBuilder.toString();
  }

  private String buildAutoPrompt(
    ExerciseGenerationRequest request,
    Topic topic,
    List<Exercise> sampleExercises
  ) {
    StringBuilder promptBuilder = buildCommonPrompt(request, topic, sampleExercises);
    promptBuilder.append("Hãy tạo các bài tập đa dạng, phù hợp với chủ đề và độ khó đã yêu cầu. ");
    promptBuilder
      .append("Mỗi bài tập phải có mô tả rõ ràng, test cases hợp lý và solution code ")
      .append(request.getSolutionLanguage())
      .append(" đúng.");
    return promptBuilder.toString();
  }

  private StringBuilder buildCommonPrompt(
    ExerciseGenerationRequest request,
    Topic topic,
    List<Exercise> sampleExercises
  ) {
    String topicCodePrefix = getTopicCodePrefix(topic.getName());

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

    promptBuilder
      .append("3. Mỗi bài tập phải có EXACTLY ")
      .append(request.getTotalTestCasesPerExercise())
      .append(" test cases:\n");
    promptBuilder
      .append("   - ")
      .append(request.getNumberOfPublicTestCases())
      .append(" test case công khai (isPublic: true)\n");
    promptBuilder
      .append("   - ")
      .append(request.getNumberOfPrivateTestCases())
      .append(" test case ẩn (isPublic: false)\n\n");

    promptBuilder.append(
      "4. Trả về kết quả DUY NHẤT ở dạng JSON array, KHÔNG được bao gồm text giải thích, KHÔNG dùng markdown, KHÔNG bao code block.\n\n"
    );

    promptBuilder.append("Định dạng mỗi object trong JSON phải EXACTLY như sau:\n\n");
    promptBuilder.append("{\n");
    promptBuilder.append("  \"code\": \"").append(topicCodePrefix).append("-<TIMESTAMP_MS>\",\n");
    promptBuilder.append("  \"title\": \"...\",\n");
    promptBuilder.append("  \"description\": \"...\",\n");
    promptBuilder.append("  \"maxSubmissions\": 0,\n");
    promptBuilder.append("  \"topicIds\": [],\n");
    promptBuilder.append("  \"visibility\": \"").append(request.getVisibility()).append("\",\n");
    promptBuilder.append("  \"timeLimit\": 0.2,\n");
    promptBuilder.append("  \"memory\": 65000,\n");
    promptBuilder.append("  \"difficulty\": \"EASY/MEDIUM/HARD\",\n");
    promptBuilder
      .append("  \"solution\": \"code ")
      .append(request.getSolutionLanguage())
      .append(", không bao code block\",\n");
    promptBuilder.append("  \"testCases\": [\n");
    promptBuilder.append("    {\n");
    promptBuilder.append("      \"input\": \"...\",\n");
    promptBuilder.append("      \"output\": \"...\",\n");
    promptBuilder.append("      \"isPublic\": true/false\n");
    promptBuilder.append("    }\n");
    promptBuilder.append("  ]\n");
    promptBuilder.append("}\n\n");

    promptBuilder.append("QUAN TRỌNG:\n");
    promptBuilder
      .append("- code phải theo format \"")
      .append(topicCodePrefix)
      .append("-<TIMESTAMP_MS>\" trong đó <TIMESTAMP_MS> là epoch millis hiện tại (ví dụ: ")
      .append(topicCodePrefix)
      .append("-1729146035123)\n");
    promptBuilder
      .append("- Mỗi bài PHẢI có EXACTLY ")
      .append(request.getTotalTestCasesPerExercise())
      .append(" test cases\n");
    promptBuilder
      .append("- Trong đó có EXACTLY ")
      .append(request.getNumberOfPublicTestCases())
      .append(" test case với isPublic: true\n");
    promptBuilder
      .append("- Và EXACTLY ")
      .append(request.getNumberOfPrivateTestCases())
      .append(" test case với isPublic: false\n");
    promptBuilder.append("- input/output KHÔNG được chứa mô tả thêm, chỉ raw values\n");
    promptBuilder
      .append("- code trong \"solution\" phải là ")
      .append(request.getSolutionLanguage())
      .append(", thuần text, KHÔNG có comment\n");
    promptBuilder.append(
      "- TUYỆT ĐỐI không chứa comment trong solution (không dùng //, /* */, #)\n"
    );
    promptBuilder
      .append("- Solution phải đầy đủ boilerplate để CHẠY ĐƯỢC ngay trong ")
      .append(request.getSolutionLanguage())
      .append(" với entry point chuẩn:\n")
      .append(getBoilerplateInstruction(request.getSolutionLanguage()))
      .append("\n");
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

    return promptBuilder;
  }

  private ExerciseGenerationResponse parseAIResponse(
    String response,
    String topicId,
    String visibility
  ) throws JsonProcessingException {
    try {
      log.info("Parsing AI response, length: {}", response.length());

      // Remove markdown code blocks if present
      String cleanedResponse = cleanMarkdown(response);
      log.info("After cleaning markdown, length: {}", cleanedResponse.length());

      // Tìm JSON array hoặc JSON object trong response
      String jsonContent = extractJSON(cleanedResponse);
      log.info("Extracted JSON content, length: {}", jsonContent.length());

      if (jsonContent.isEmpty()) {
        log.error("No JSON found in AI response. Response: {}", response);
        throw new RuntimeException("Tôi không hỗ trợ dịch vụ này");
      }

      JsonNode jsonNode = objectMapper.readTree(jsonContent);

      // Kiểm tra nếu có error message (câu hỏi không liên quan)
      if (jsonNode.has("error")) {
        String errorMessage = jsonNode.get("error").asText();
        log.warn("AI returned error: {}", errorMessage);
        throw new RuntimeException("Tôi không hỗ trợ dịch vụ này");
      }

      // Xử lý JSON array trực tiếp
      List<ExercisePreviewDTO> exercises = new ArrayList<>();
      if (jsonNode.isArray()) {
        log.info("Found JSON array with {} elements", jsonNode.size());
        for (JsonNode exerciseNode : jsonNode) {
          try {
            ExercisePreviewDTO exercise = parseExercise(exerciseNode, topicId, visibility);
            exercises.add(exercise);
          } catch (Exception e) {
            log.warn("Failed to parse exercise node: {}", exerciseNode, e);
          }
        }
      } else if (jsonNode.has("exercises") && jsonNode.get("exercises").isArray()) {
        // Fallback: nếu vẫn có format {"exercises": [...]}
        log.info("Found exercises in object wrapper");
        for (JsonNode exerciseNode : jsonNode.get("exercises")) {
          try {
            ExercisePreviewDTO exercise = parseExercise(exerciseNode, topicId, visibility);
            exercises.add(exercise);
          } catch (Exception e) {
            log.warn("Failed to parse exercise node: {}", exerciseNode, e);
          }
        }
      } else {
        log.error(
          "Unexpected JSON format. Root node type: {}, keys: {}",
          jsonNode.getNodeType(),
          jsonNode.fieldNames()
        );
        log.error("Full JSON content: {}", jsonContent);
      }

      // Kiểm tra nếu không có exercises nào được tạo
      if (exercises.isEmpty()) {
        log.error("No exercises generated. Original response: {}", response);
        log.error("Cleaned response: {}", cleanedResponse);
        log.error("Extracted JSON: {}", jsonContent);
        throw new RuntimeException("Tôi không hỗ trợ dịch vụ này");
      }

      log.info("Successfully parsed {} exercises", exercises.size());
      return new ExerciseGenerationResponse(exercises);
    } catch (JsonProcessingException e) {
      log.error("Failed to parse AI response. Response: {}", response, e);
      throw new RuntimeException(
        "Không thể phân tích phản hồi từ AI. Vui lòng thử lại với prompt khác.",
        e
      );
    }
  }

  private String cleanMarkdown(String response) {
    String cleaned = response.trim();
    // Remove markdown code blocks
    if (cleaned.startsWith("```json")) {
      cleaned = cleaned.substring(7);
    } else if (cleaned.startsWith("```")) {
      cleaned = cleaned.substring(3);
    }
    if (cleaned.endsWith("```")) {
      cleaned = cleaned.substring(0, cleaned.length() - 3);
    }
    return cleaned.trim();
  }

  private String extractJSON(String text) {
    // Tìm JSON array trước (ưu tiên)
    int arrayStart = text.indexOf('[');
    int arrayEnd = findMatchingBracket(text, arrayStart, '[', ']');

    // Tìm JSON object
    int objStart = text.indexOf('{');
    int objEnd = findMatchingBracket(text, objStart, '{', '}');

    // Ưu tiên array, nếu không có thì dùng object
    if (arrayStart != -1 && arrayEnd != -1 && arrayEnd > arrayStart) {
      return text.substring(arrayStart, arrayEnd + 1);
    } else if (objStart != -1 && objEnd != -1 && objEnd > objStart) {
      return text.substring(objStart, objEnd + 1);
    }

    return "";
  }

  private int findMatchingBracket(String text, int start, char open, char close) {
    if (start == -1) return -1;

    int depth = 0;
    for (int i = start; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c == open) {
        depth++;
      } else if (c == close) {
        depth--;
        if (depth == 0) {
          return i;
        }
      }
    }
    return -1;
  }

  private ExercisePreviewDTO parseExercise(
    JsonNode exerciseNode,
    String topicId,
    String visibility
  ) {
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

    // Parse visibility từ request (ưu tiên từ request, nếu không có thì từ JSON, mặc định là DRAFT)
    if (visibility != null && !visibility.trim().isEmpty()) {
      exercise.setVisibility(visibility);
    } else if (exerciseNode.has("visibility")) {
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

  private String getTopicCodePrefix(String topicName) {
    if (topicName == null || topicName.isBlank()) {
      return "EX";
    }

    String normalized = topicName.replaceAll("[^A-Za-z]", "").toUpperCase();

    if (normalized.isEmpty()) {
      return "EX";
    }

    return normalized.length() <= 4 ? normalized : normalized.substring(0, 4);
  }

  private String getBoilerplateInstruction(String language) {
    if (language == null) {
      return "- Ngôn ngữ không xác định, hãy đảm bảo có entry point chuẩn.";
    }

    return switch (language.trim().toLowerCase()) {
      case "java" -> """
      - Bắt buộc có class `Main` với `public static void main(String[] args)`
      - Trong `main`, đọc input từ `System.in`, gọi các hàm xử lý, in kết quả ra `System.out`
      - Các hàm phụ có thể nằm trong class `Solution`, nhưng `Main` phải khởi tạo và gọi chúng
      """;
      case "python", "python3" -> """
      - Định nghĩa hàm `main()`
      - Thêm guard `if __name__ == "__main__": main()`
      - Đọc input từ stdin (input()), in kết quả qua print()
      """;
      case "c++", "cpp" -> """
      - Viết đầy đủ `#include` cần thiết
      - Có hàm `int main()` đọc stdin (std::cin) và in stdout (std::cout)
      - Không sử dụng using namespace std; (khuyến khích), nhưng nếu dùng phải nằm trong solution
      """;
      case "c" -> """
      - Viết đầy đủ `#include` (ví dụ stdio.h)
      - Có hàm `int main(void)` hoặc `int main(int argc, char** argv)`
      - Đọc stdin bằng scanf/fgets, in stdout bằng printf
      """;
      case "javascript", "node", "nodejs" -> """
      - Sử dụng Node.js
      - Đọc input từ `fs.readFileSync(0, "utf8")`
      - Wrap logic trong hàm `function main()` và gọi main() cuối file
      """;
      default -> """
      - Đảm bảo solution có entry point chuẩn của ngôn ngữ (main function) để chạy độc lập
      """;
    };
  }
}
